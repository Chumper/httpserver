package io.github.chumper.webserver.handler;

import org.junit.Before;
import org.junit.Test;

import io.github.chumper.webserver.core.http.HttpBase;
import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpHeaders;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class HttpFileHandlerTest {

  private HttpHandler handler = new HttpFileHandler("src/test/resources");

  private HttpResponse response;
  private HttpRequest request;

  @Before
  public void setUp() throws Exception {
    this.request = new HttpRequest();
    this.response = new HttpResponse();
  }

  @Test
  public void testFileC() {

    request.setMethod(HttpBase.Method.GET);
    request.setPath("/files/c.txt");

    handler.handle(request, response);

    assertEquals("c", response.getContent().toString());

    assertNotNull(response.getHeaders().get(HttpHeaders.ETAG));
    assertNotNull(response.getHeaders().get(HttpHeaders.LAST_MODIFIED));

  }

  @Test
  public void testFileB() {

    request.setMethod(HttpBase.Method.GET);
    request.setPath("/files//a/b.txt");

    handler.handle(request, response);

    assertEquals("abc", response.getContent().toString());

    assertNotNull(response.getHeaders().get(HttpHeaders.ETAG));
    assertNotNull(response.getHeaders().get(HttpHeaders.LAST_MODIFIED));

  }

  @Test
  public void testDirectory() {

    request.setMethod(HttpBase.Method.GET);
    request.setPath("/files");

    handler.handle(request, response);

    assertEquals("a\r\nc.txt\r\n", response.getContent().toString());

    assertNotNull(response.getHeaders().get(HttpHeaders.ETAG));
    assertNotNull(response.getHeaders().get(HttpHeaders.LAST_MODIFIED));

  }

  @Test
  public void testDirectoryNotFound() {

    request.setMethod(HttpBase.Method.GET);
    request.setPath("/files/b");

    handler.handle(request, response);

    assertEquals("404 Not found", response.getStatus());

    assertNull(response.getHeaders().get(HttpHeaders.ETAG));
    assertNull(response.getHeaders().get(HttpHeaders.LAST_MODIFIED));

  }
}