package io.github.chumper.webserver.core;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.chumper.webserver.core.pipeline.SocketHandler;
import io.github.chumper.webserver.util.ConsoleLogger;
import io.github.chumper.webserver.util.Logger;

/**
 * This is the base class for all servers. Subclasses need to populate the pipeline with their
 * custom handlers and can overwrite methods if needed
 */
public abstract class Server {

  /**
   * The port the server will listen on
   */
  private int port;
  /**
   * Used to start the server async and provide a method to listen when the server started and is
   * ready to accept requests
   */
  private CountDownLatch startLatch = new CountDownLatch(1);
  /**
   * The executor that is responsible to process each request, this is the worker pool
   */
  private ExecutorService executor;
  /**
   * The socked that accepts the incomming messages
   */
  private ServerSocket ss;
  /**
   * Simple logger to log all events
   */
  private Logger logger = new ConsoleLogger();
  /**
   * The list of handlers that will transform the request and do stuff like HTTP parsing and
   * processing
   */
  private List<SocketHandler> socketHandlers = new ArrayList<>();

  /**
   * Creates a server that will listen on the given port when started
   *
   * @param port    The port number to listen on
   * @param threads The number of threads for the worker pool
   */
  public Server(int port,
                int threads) {
    this.port = port;
    this.executor = Executors.newFixedThreadPool(threads);
  }

  /**
   * Will add the given handler to the server processing pipeline
   *
   * @param handler The handler that transforms the request
   */
  public void addPipelineHandler(SocketHandler handler) {
    this.socketHandlers.add(handler);
  }

  /**
   * Will start the server in a new thread and returns a future that will complete when the server
   * has been started
   *
   * @return A Future that completes when the server has been started
   */
  public CompletableFuture<Boolean> start() {
    logger.log("Starting server on port {}", port);

    registerHandler();

    SocketListenerThread socketListener = new SocketListenerThread(port);
    socketListener.start();

    return CompletableFuture.supplyAsync(() -> {
      try {
        startLatch.await();
      } catch (InterruptedException e) {
        logger.log("Error while server start: {}", e.getMessage());
      }
      return socketListener.isBound();
    });
  }

  /**
   * Will stop the server and closes the sockets and shutdown the worker threads. All buffered
   * request will be discarded and no new connections will be accepted.
   */
  public void stop() throws IOException {

    ss.close();
    executor.shutdown();

    logger.log("Server stopped");
  }

  private class SocketListenerThread
      extends Thread {

    private int port;

    SocketListenerThread(int port) {
      this.port = port;
      setName("SocketListenerThread");
    }

    public void run() {
      try {
        ss = new ServerSocket();
        ss.setReuseAddress(true);
        ss.setSoTimeout(0);
        ss.bind(new InetSocketAddress(port), 20000);

        logger.log("Server started on port {}, waiting for connections", port);

        // count down the latch so that we can indicate that the server started
        startLatch.countDown();

        acceptData();
      } catch (IOException e) {
        // we dont want to clutter the console with stacktraces in this simple exercise
        logger.log("Could not open the socket on port {}: {}", port, e.getMessage());
        startLatch.countDown();
      }
    }

    private void acceptData() {
      while (true) {
        try {
          if (executor.isTerminated()) { break; }

          executor.execute(new SocketProcessor(ss.accept(), socketHandlers));

        } catch (IOException e) {
          // I/O error in reading/writing data, or server closed while
          // accepting data
          if (!executor.isTerminated() && !ss.isClosed()) {
            logger.log("Error occurred: {}", e.getMessage());
          }
        }
      }
    }

    /**
     * Will check whether the serverSocket is bound or not
     *
     * @return boolean true if the socket is bound, false otherwise
     */
    boolean isBound() {
      return ss.isBound();
    }
  }

  /**
   * This methods needs to be overriden by other subclasses where they implement their own handler
   * pipeline
   */
  protected abstract void registerHandler();
}