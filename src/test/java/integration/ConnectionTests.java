package integration;

import org.junit.Test;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutionException;

import integration.util.ServerTest;
import io.github.chumper.webserver.core.HttpServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Will test if the server handles the connection correctly
 */
public class ConnectionTests
    extends ServerTest {

  /**
   * Will test that a connection to the server can be established and will timeout if nothing has
   * been send for 3 seconds
   */
  @Test
  public void testTimeOutConnection() throws IOException, InterruptedException {
    try (Socket socket = new Socket("localhost", serverPort)) {
      assertFalse(socket.isClosed());
      Thread.sleep(4000);

      // we try to read from the input stream which should return -1 which means the connection is
      // closed
      assertEquals(-1, socket.getInputStream().read());
    }
  }

  /**
   * Tests that the server can not be spawned on the same port
   */
  @Test
  public void testPortInUse() throws IOException, InterruptedException, ExecutionException {
    assertFalse(new HttpServer(serverPort, 1).start().get());
  }

  /**
   * Will test that a connection to the server will be closed when the clients does not send a
   * formal HTTP request
   */
  @Test
  public void testCloseInformalHttpRequest() throws IOException, InterruptedException {
    try (Socket socket = new Socket("localhost", serverPort)) {
      assertFalse(socket.isClosed());

      // send foobar to the server
      socket.getOutputStream().write("FoBaR\r\n".getBytes(Charset.defaultCharset()));
      socket.getOutputStream().flush();

      // try to read from the stream
      assertEquals(-1, socket.getInputStream().read());

    }
  }
}
