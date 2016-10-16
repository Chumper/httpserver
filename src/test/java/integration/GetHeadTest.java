package integration;

import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.Test;

import java.io.IOException;

import integration.util.ServerTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Will test GET and HEAD requests
 */
public class GetHeadTest extends ServerTest {

  @Test
  public void testGet() throws IOException {

    Response response = Request.Get(serverUrl).execute();

    HttpResponse resp = response.returnResponse();

    assertEquals(200, resp.getStatusLine().getStatusCode());
    assertTrue(resp.getEntity().getContentLength() > 0);
  }

  @Test
  public void testHead() throws IOException {

    Response response = Request.Head(serverUrl).execute();

    HttpResponse resp = response.returnResponse();

    assertEquals(200, resp.getStatusLine().getStatusCode());
    assertNull(resp.getEntity());
  }

}
