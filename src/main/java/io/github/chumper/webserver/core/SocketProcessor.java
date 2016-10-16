package io.github.chumper.webserver.core;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;

import io.github.chumper.webserver.core.pipeline.SocketHandler;
import io.github.chumper.webserver.util.ConsoleLogger;
import io.github.chumper.webserver.util.Logger;

/**
 * The main thread that processes a socket, will handle the handler and the lifecycle
 */
class SocketProcessor
    implements Runnable {

  private final Logger logger = new ConsoleLogger();

  /**
   * The socket to manage
   */
  private Socket socket;
  /**
   * A list of handlers that can manipulate the socket and interact with the manipulated request,
   * similar to the netty pipeline but much more simple and less robust.
   */
  private List<SocketHandler> socketHandlers;

  SocketProcessor(Socket socket,
                  List<SocketHandler> socketHandlers) {
    this.socket = socket;
    this.socketHandlers = socketHandlers;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void run() {
    try {
      // first we set a timeout so inactive sockets will not stop the server after a while
      socket.setSoTimeout(3000);
      // we will loop through all handlers until one closes the socket
      while(!socket.isClosed()) {
        SocketHandler.State state;
        for (SocketHandler socketHandler : socketHandlers) {
          state = socketHandler.process(socket);
          // when a handler returns no response we will asume that the pipeline should be interrupted
          // This could be more robust (e.g. exceptions or a pipeline status object) but should be enough for now.
          if (state == SocketHandler.State.DISCARD) {
            // closing of the socket is not the responsible of the handler it will be done in the
            // finally block
            return;
          }
        }
      }
    } catch (SocketException e) {
      logger.log("Could not configure the socket: {}", e.getMessage());
    } finally {
      try {
        // just in case a handler did not clean, we will do it here
        socket.shutdownInput();
        socket.shutdownOutput();
        socket.close();
      } catch (IOException e) {
        logger.log("Could not close the socket: {}", e.getMessage());
      }
    }
  }
}
