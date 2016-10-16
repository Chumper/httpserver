package integration;

import org.apache.http.HttpHeaders;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.nio.charset.Charset;

import integration.util.ServerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

/**
 * Will test the keep alive support of the server but only with HTTP 1.0 and HTTP 1.1
 * Normally you should also test 0.9 but i will leave that for another exercise
 *
 * @see https://tools.ietf.org/html/rfc2616#section-8.1.2
 */
public class KeepAliveTest
    extends ServerTest {

  /**
   * Will test that the server responds with a valid HTTP Request when a valid request is send
   */
  @Test
  public void validRequest() throws IOException {

    Response resp = Request.Get(serverUrl).execute();

    assertEquals(200, resp.returnResponse().getStatusLine().getStatusCode());

  }

  /**
   * Will test that when close keep alive is set with HTTP 1.0 that the server responds with the
   * correct headers and closes the connection.
   */
  @Test
  public void http10NoKeepAlive() throws IOException {

    try (Socket socket = new Socket("localhost", serverPort)) {
      assertFalse(socket.isClosed());

      socket.getOutputStream().write(keepAlive("1.0", "close").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      // get the response and assert that the response is correct
      TestResponse response = readResponse(socket);

      assertEquals(200, response.getStatusCode());
      assertEquals("close", response.getHeaders().get(HttpHeaders.CONNECTION).toLowerCase());

      // assert that the socket is closed
      BufferedReader reader =
          new BufferedReader(new InputStreamReader(socket.getInputStream()));
      assertEquals(-1, socket.getInputStream().read());
    }

  }

  /**
   * Will test that when keep alive is set with HTTP 1.0 that the server responds with the correct
   * headers and leaves the connection open.
   */
  @Test
  public void http10KeepAlive() throws IOException {

    try (Socket socket = new Socket("localhost", serverPort)) {
      assertFalse(socket.isClosed());

      socket.getOutputStream().write(keepAlive("1.0", "keep-alive").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      // get the response and assert that the response is correct
      TestResponse response = readResponse(socket);

      assertEquals(200, response.getStatusCode());
      assertEquals("keep-alive", response.getHeaders().get(HttpHeaders.CONNECTION).toLowerCase());

      // assert that the socket is not by sending another request
      socket.getOutputStream().write(keepAlive("1.0", "keep-alive").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      response = readResponse(socket);

      assertNotNull(response);
      assertEquals(200, response.getStatusCode());
    }

  }

  /**
   * Default 1.0 http request with no header set should close the connection after
   */
  @Test
  public void defaultHttp10KeepAlive() throws IOException {
    try (Socket socket = new Socket("localhost", serverPort)) {
      assertFalse(socket.isClosed());

      socket.getOutputStream().write(get("1.0").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      // get the response and assert that the response is correct
      TestResponse response = readResponse(socket);

      assertEquals(200, response.getStatusCode());
      assertEquals("close", response.getHeaders().get(HttpHeaders.CONNECTION).toLowerCase());

      // assert that the socket is closed
      assertEquals(-1, socket.getInputStream().read());
    }
  }

  /**
   * Will test that when keep alive is set to close with HTTP 1.1 that the server responds with the
   * correct headers and closes the connection open.
   */
  @Test
  public void http11NoKeepAlive() throws IOException {

    try (Socket socket = new Socket("localhost", serverPort)) {
      assertFalse(socket.isClosed());

      socket.getOutputStream().write(keepAlive("1.1", "close").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      // get the response and assert that the response is correct
      TestResponse response = readResponse(socket);

      assertEquals(200, response.getStatusCode());
      assertEquals("close", response.getHeaders().get(HttpHeaders.CONNECTION).toLowerCase());

      // assert that the socket is closed
      assertEquals(-1, socket.getInputStream().read());
    }

  }

  /**
   * Will test that when keep alive is set with HTTP 1.1 that the server responds with the correct
   * headers and leaves the connection open.
   */
  @Test
  public void http11KeepAlive() throws IOException {

    try (Socket socket = new Socket("localhost", serverPort)) {
      assertFalse(socket.isClosed());

      socket.getOutputStream().write(keepAlive("1.1", "keep-alive").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      // get the response and assert that the response is correct
      TestResponse response = readResponse(socket);

      assertEquals(200, response.getStatusCode());
      assertEquals("keep-alive", response.getHeaders().get(HttpHeaders.CONNECTION).toLowerCase());

      // assert that the socket is not by sending another request
      socket.getOutputStream().write(keepAlive("1.1", "keep-alive").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      response = readResponse(socket);

      assertNotNull(response);
      assertEquals(200, response.getStatusCode());
    }

  }

  /**
   * Will test that when keep alive is not set with HTTP 1.1 that the server responds with the
   * correct headers and leaves the connection open.
   */
  @Test
  public void defaultHttp11KeepAlive() throws IOException {

    try (Socket socket = new Socket("localhost", serverPort)) {
      assertFalse(socket.isClosed());

      socket.getOutputStream().write(get("1.1").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      // get the response and assert that the response is correct
      TestResponse response = readResponse(socket);

      assertEquals(200, response.getStatusCode());
      assertEquals("keep-alive", response.getHeaders().get(HttpHeaders.CONNECTION).toLowerCase());

      // assert that the socket is not by sending another request
      socket.getOutputStream().write(keepAlive("1.1", "keep-alive").getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      response = readResponse(socket);

      assertNotNull(response);
      assertEquals(200, response.getStatusCode());
    }

  }

  private String keepAlive(String version,
                           String keepAlive) {
    return "GET / HTTP/" + version + "\r\n"
           + "Connection: " + keepAlive + "\r\n"
           + "\r\n";
  }

  private String get(String version) {
    return "GET / HTTP/" + version + "\r\n"
           + "\r\n";
  }
}
