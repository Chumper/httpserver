package io.github.chumper.webserver.handler;

import java.io.IOException;

import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpHeaders;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;

/**
 * Will set the correct connection header based on the keep alive request header
 */
public class HttpKeepAliveHandler implements HttpHandler {

  @Override
  public void handle(HttpRequest request,
                     HttpResponse response) {

    // Add the keep alive header in both cases
    if(request.isKeepAlive()) {
      response.getHeaders().add(HttpHeaders.CONNECTION, "keep-alive");
    } else {
      response.getHeaders().add(HttpHeaders.CONNECTION, "close");
    }

  }
}
