package io.github.chumper.webserver.handler;

import io.github.chumper.webserver.core.http.HttpBase;
import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;

/**
 * Will return urls to the file listing and the comment listing
 */
public class HttpRootHandler
    implements HttpHandler {

  @Override
  public void handle(HttpRequest request,
                     HttpResponse response) {

    if(request.getMethod() == HttpBase.Method.GET || request.getMethod() == HttpBase.Method.HEAD) {
      if(request.getPath().equals("/")) {
        response.write("Files   : /files\r\n");
        response.write("Comments: /comments\r\n");
      }
    }
  }
}
