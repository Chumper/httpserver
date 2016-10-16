package io.github.chumper.webserver.core.http;

/**
 * The {@link HttpHandler} interface should be implemented by all classes that want to handle http requests.
 * It provides the request and a response object that can be populated by the handler
 */
public interface HttpHandler {
  /**
   * The handle method will be called as soon as a http request arrives at the server.
   * @param request The incoming {@link HttpRequest}
   * @param response The response that will be returned to the client
   */
  void handle(HttpRequest request, HttpResponse response);
}
