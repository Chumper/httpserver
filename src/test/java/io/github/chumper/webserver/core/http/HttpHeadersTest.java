package io.github.chumper.webserver.core.http;

import static org.junit.Assert.*;

/**
 * Will test if headers can set correctly
 */
public class HttpHeadersTest {

  public void testHeaderParse() {

  }

  public void testHeaderDecode() {

  }

  private String exampleRequest() {
    return "GET /foobar HTTP/1.1\n"
           + "Accept: */*\n"
           + "Accept-Encoding: gzip, deflate\n"
           + "Connection: keep-alive\n"
           + "Host: localhost:8080\n"
           + "If-Match: 1475154057000\n"
           + "User-Agent: ExampleAgent";
  }

}