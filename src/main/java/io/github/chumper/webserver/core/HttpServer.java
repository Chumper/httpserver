package io.github.chumper.webserver.core;

import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.pipeline.HttpPipeline;

/**
 * The {@link HttpServer} extends the {@link Server} and adds handler that will interpret the
 * incoming messages as HTTP requests.
 */
public class HttpServer
    extends Server {

  /**
   * The {@link HttpServer} will add a {@link HttpPipeline} to the server and accepts custom {@link
   * HttpHandler} for processing the {@link HttpRequest}
   */
  private HttpPipeline httpPipeline = new HttpPipeline();

  /**
   * Creates a server that will listen on the given port when started
   *
   * @param port    The port number to listen on
   * @param threads The number of threads for the worker pool
   */
  public HttpServer(int port,
                    int threads) {
    super(port, threads);
  }

  /**
   * Will add the given handler to the Http pipeline
   */
  public void addHttpHandler(HttpHandler httpHandler) {
    this.httpPipeline.addHttpHandler(httpHandler);
  }

  @Override
  protected void registerHandler() {
    // configure the server with the HTTP Pipeline
    addPipelineHandler(httpPipeline);
  }
}
