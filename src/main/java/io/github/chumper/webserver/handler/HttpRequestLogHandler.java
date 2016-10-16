package io.github.chumper.webserver.handler;

import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;
import io.github.chumper.webserver.util.ConsoleLogger;
import io.github.chumper.webserver.util.Logger;

/**
 * Handler that just logs parts of the http request and parts of the response
 */
public class HttpRequestLogHandler implements HttpHandler {

  private final Logger logger = new ConsoleLogger();

  @Override
  public void handle(HttpRequest request,
                     HttpResponse response) {

    logger.log("{}: {} -> {}", request.getMethod(), response.status(), request.getPath());
  }
}
