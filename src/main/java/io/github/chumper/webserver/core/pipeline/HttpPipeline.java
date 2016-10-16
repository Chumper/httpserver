package io.github.chumper.webserver.core.pipeline;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import io.github.chumper.webserver.core.HttpServer;
import io.github.chumper.webserver.core.http.HttpBase;
import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpHeaders;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;
import io.github.chumper.webserver.util.ConsoleLogger;
import io.github.chumper.webserver.util.Logger;

/**
 * The {@link HttpPipeline} is the central piece of the provided {@link HttpServer}.
 * It will transform an incoming connection into an {@link HttpRequest} if possible, otherwise
 * it will reject the request.
 * It will call all registered {@link HttpHandler} and will return the response in the end.
 * If the connection should be closed it will close the socket.
 */
public class HttpPipeline implements SocketHandler {

  private final Logger logger = new ConsoleLogger();

  /**
   * All handlers that are interested in incoming {@link HttpRequest}
   */
  private List<HttpHandler> handlers = new ArrayList<>();

  /**
   * Add the given handler to the list of handlers that will be called when a http request is
   * incoming.
   * @param handler The handler to add.
   */
  public void addHttpHandler(HttpHandler handler) {
    this.handlers.add(handler);
  }

  @Override
  public State process(Socket socket) {
    try {
      // convert to easier to read stream
      BufferedReader r = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      Optional<HttpRequest> request = createRequest(r);

      if (!request.isPresent()) {
        // cant read the request, so discard this message and close the connection
        return State.DISCARD;
      }

      // create the response object
      HttpResponse response = new HttpResponse();

      // pass it to the handlers
      for (HttpHandler handler : this.handlers) {
        handler.handle(request.get(), response);
      }

      // send the response
      sendResponse(request.get(), response, socket.getOutputStream());

      // if it should kept open, then advice the socket processor to not close the socket
      if(request.get().isKeepAlive()) {
        return State.CONTINUE;
      }
    } catch (SocketTimeoutException e) {
      logger.log("Socket was inactive for too long, closing {}", socket.toString());
    } catch (IOException e) {
      logger.log("Error while converting the HttpInputStream to a HttpRequest: ", e.getMessage());
    }
    return State.DISCARD;
  }

  /**
   * Will send the {@link HttpResponse} to the client
   */
  private void sendResponse(HttpRequest request,
                            HttpResponse response,
                            OutputStream outputStream) throws IOException {

    // write headers
    outputStream.write((response.getProtocol() + " " + response.getStatus() + "\r\n").getBytes());
    outputStream.write(("Date: " + HttpHeaders.headerTime.format(new Date()) + "\r\n").getBytes());
    outputStream.write(("Server: SimpleHttpServer\r\n").getBytes());
    outputStream.write(("Content-Length: " + response.getContentLength() + "\r\n").getBytes());

    for (Map.Entry<String, String> header : response.getHeaders().get()) {
      outputStream.write((header.getKey() + ": " + header.getValue() + "\r\n").getBytes());
    }

    outputStream.write("\r\n".getBytes());

    // write content if available
    if (response.getContentLength() > 0 && request.getMethod() != HttpBase.Method.HEAD) {
      outputStream.write(response.getContent().toByteArray());
    }

    // flush the response
    outputStream.flush();
  }

  /**
   * Will try to create an {@link HttpRequest} from the incomming connection, if not possible then
   * the socket will be closed.
   */
  private Optional<HttpRequest> createRequest(BufferedReader reader) throws IOException {
    HttpRequest request = new HttpRequest();
    List<String> headers = new ArrayList<>();

    String line = reader.readLine();
    if (line == null || !line.matches("\\w+ /.* HTTP/.*")) {
      // reject request as the first line is not a valid header or the client closed the connection
      return Optional.empty();
    }
    while (reader.ready()) {
      if (line.equals("")) { break; }
      headers.add(line);
      line = reader.readLine();
    }

    if (headers.isEmpty()) {
      return Optional.empty();
    }

    String rawFirstLine = headers.remove(0);
    String[] methodAndPath = rawFirstLine.split(" ");

    request.setMethod(HttpBase.Method.valueOf(methodAndPath[0]));
    request.setPath(methodAndPath[1]);

    switch (methodAndPath[2]) {
      case "HTTP/1.1":
        request.setProtocol(HttpBase.Version.HTTP_1_1);
        break;
      case "HTTP/1.0":
        request.setProtocol(HttpBase.Version.HTTP_1_0);
        break;
    }

    headers.forEach(it -> request.getHeaders().add(it));

    if(request.getHeaders().get(HttpHeaders.CONTENT_LENGTH) != null) {
      int read;
      StringBuilder b = new StringBuilder();
      while ((read = reader.read()) != -1) {
        b.append((char) read);
        if (b.length() == Integer.parseInt(request.getHeaders().get(HttpHeaders.CONTENT_LENGTH)))
          break;
      }
      request.setContent(b.toString());
    }

    return Optional.of(request);
  }
}
