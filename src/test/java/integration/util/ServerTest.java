package integration.util;

import org.junit.After;
import org.junit.Before;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import io.github.chumper.webserver.core.HttpServer;
import io.github.chumper.webserver.core.http.HttpHeaders;
import io.github.chumper.webserver.handler.HttpCommentHandler;
import io.github.chumper.webserver.handler.HttpETagHandler;
import io.github.chumper.webserver.handler.HttpFileHandler;
import io.github.chumper.webserver.handler.HttpKeepAliveHandler;
import io.github.chumper.webserver.handler.HttpRequestLogHandler;
import io.github.chumper.webserver.handler.HttpRootHandler;
import io.github.chumper.webserver.util.InMemoryRepo;

/**
 * Base class for the integration tests, will start a new server and tear it down afterwards
 */
public class ServerTest {

  private HttpServer server;

  protected int serverPort = 8888;

  protected String serverUrl = "http://localhost:" +serverPort + "/";

  @Before
  public void setUp() throws Exception {
    this.server = new HttpServer(8888, 1);

    this.server.addHttpHandler(new HttpRootHandler());

    this.server.addHttpHandler(new HttpCommentHandler(new InMemoryRepo()));

    this.server.addHttpHandler(new HttpFileHandler(""));
    this.server.addHttpHandler(new HttpETagHandler());

    this.server.addHttpHandler(new HttpKeepAliveHandler());
    this.server.addHttpHandler(new HttpRequestLogHandler());

    // start the server and wait until it is started
    this.server.start().get();
  }

  @After
  public void tearDown() throws Exception {
    this.server.stop();
  }

  protected TestResponse readResponse(Socket s) throws IOException {

    List<String> headers = new ArrayList<>();

    BufferedReader reader = new BufferedReader(new InputStreamReader(s.getInputStream()));

    String line = reader.readLine();

    while(!line.equals("")) {
      headers.add(line);
      line = reader.readLine();
    }

    if(headers.isEmpty()) {
      assert false;
    }

    TestResponse testResponse = new TestResponse();

    String rawFirstLine = headers.remove(0);
    String[] methodAndPath = rawFirstLine.split(" ");

    testResponse.setStatusCode(Integer.parseInt(methodAndPath[1]));

    headers.forEach(testResponse::addHeader);

    // read content as stated in the header and discard it :)
    if(Integer.parseInt(testResponse.getHeaders().get("Content-Length")) > 0) {
      char[] content = new char[Integer.parseInt(testResponse.getHeaders().get("Content-Length"))];
      reader.read(content, 0, Integer.parseInt(testResponse.getHeaders().get("Content-Length")));
    }

    return testResponse;
  }

  public static class TestResponse {
    private HttpHeaders headers = new HttpHeaders();
    private int statusCode;
    public void addHeader(String rawHeaderString) {
      headers.add(rawHeaderString);
    }

    public void setStatusCode(int statusCode) {
      this.statusCode = statusCode;
    }

    public int getStatusCode() {
      return statusCode;
    }

    public HttpHeaders getHeaders() {
      return headers;
    }
  }

}
