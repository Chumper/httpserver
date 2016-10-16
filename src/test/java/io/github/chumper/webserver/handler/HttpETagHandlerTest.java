package io.github.chumper.webserver.handler;

import org.junit.Before;
import org.junit.Test;

import io.github.chumper.webserver.core.http.HttpBase;
import io.github.chumper.webserver.core.http.HttpHeaders;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class HttpETagHandlerTest {

  private HttpETagHandler handler = new HttpETagHandler();

  private HttpResponse response;
  private HttpRequest request;

  @Before
  public void setUp() throws Exception {
    this.request = new HttpRequest();
    this.response = new HttpResponse();
  }

  @Test
  public void testNoEtag() {

    handler.handle(request, response);

    assertNull(response.getHeaders().get(HttpHeaders.ETAG));
  }

  @Test
  public void testEtag() {

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));
  }

  // if match false -> 412
  @Test
  public void IfMatchFalse() {

    request.getHeaders().add(HttpHeaders.IF_MATCH, "123123");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("412 Precondition failed", response.getStatus());
  }

  @Test
  public void IfMatchTrueWildCard() {

    request.getHeaders().add(HttpHeaders.IF_MATCH, "*");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("200 OK", response.getStatus());
  }

  @Test
  public void IfMatchFalseMultiple() {

    request.getHeaders().add(HttpHeaders.IF_MATCH, "\"123123\", \"asdasd\"");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("412 Precondition failed", response.getStatus());
  }

  // if match true, if-none-match true -> 200
  @Test
  public void IfMatchTrueIfNoneMatchTrue() {

    request.getHeaders().add(HttpHeaders.IF_MATCH, "123");
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "1234");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("200 OK", response.getStatus());
  }

  // if match true, if-none-match false GET -> 304
  @Test
  public void IfMatchTrueIfNoneMatchFalseGet() {

    request.setMethod(HttpBase.Method.GET);
    request.getHeaders().add(HttpHeaders.IF_MATCH, "123");
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }

  // if match true, if-none-match false HEAD-> 304
  @Test
  public void IfMatchTrueIfNoneMatchFalseHead() {

    request.setMethod(HttpBase.Method.HEAD);
    request.getHeaders().add(HttpHeaders.IF_MATCH, "123");
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }

  // if match true, if-none-match false POST -> 412
  @Test
  public void IfMatchTrueIfNoneMatchFalsePost() {

    request.setMethod(HttpBase.Method.POST);
    request.getHeaders().add(HttpHeaders.IF_MATCH, "123");
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("412 Precondition failed", response.getStatus());
  }

  // if unmodified since false -> 412
  @Test
  public void IfUnmodifiedSinceFalse() {

    request.getHeaders().add(HttpHeaders.IF_UNMODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");

    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:58 CEST");
    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("412 Precondition failed", response.getStatus());
  }

  // if unmodified since true, if none match false, get -> 304
  @Test
  public void IfUnmodifiedSinceTrueIfNoneMatchFalseGet() {

    request.setMethod(HttpBase.Method.GET);
    request.getHeaders().add(HttpHeaders.IF_UNMODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:56 CEST");
    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }

  // if unmodified since true, if none match false, head -> 304
  @Test
  public void IfUnmodifiedSinceTrueIfNoneMatchFalseHead() {

    request.setMethod(HttpBase.Method.HEAD);
    request.getHeaders().add(HttpHeaders.IF_UNMODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:56 CEST");
    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }

  // if unmodified since true, if none match false, post -> 412
  @Test
  public void IfUnmodifiedSinceTrueIfNoneMatchFalsePOS() {

    request.setMethod(HttpBase.Method.POST);
    request.getHeaders().add(HttpHeaders.IF_UNMODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:56 CEST");
    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("412 Precondition failed", response.getStatus());
  }

  // if unmodified since true, if none match true -> 200
  @Test
  public void IfUnmodifiedSinceTrueIfNoneMatchTrue() {

    request.getHeaders().add(HttpHeaders.IF_UNMODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "1231");

    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:56 CEST");
    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("200 OK", response.getStatus());
  }

  // if none match false get -> 304
  @Test
  public void IfNoneMatchFalseGet() {

    request.setMethod(HttpBase.Method.GET);
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }

  @Test
  public void IfNoneMatchFalseGetWildcard() {

    request.setMethod(HttpBase.Method.GET);
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "*");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }

  // if none match false head -> 304
  @Test
  public void IfNoneMatchFalseHead() {

    request.setMethod(HttpBase.Method.HEAD);
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }

  // if none match false post -> 412
  @Test
  public void IfNoneMatchFalsePost() {

    request.setMethod(HttpBase.Method.POST);
    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "123");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("412 Precondition failed", response.getStatus());
  }

  // if none match true -> 200
  @Test
  public void IfNoneMatchTrue() {

    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "1234");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("200 OK", response.getStatus());
  }

  @Test
  public void IfNoneMatchTrueMultiple() {

    request.getHeaders().add(HttpHeaders.IF_NONE_MATCH, "\"1234\", \"12345\"");

    response.getHeaders().add(HttpHeaders.ETAG, "123");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("200 OK", response.getStatus());
  }

  // if get, if modified since true -> 200
  @Test
  public void GetIfModifiedSinceTrue() {

    request.setMethod(HttpBase.Method.GET);
    request.getHeaders().add(HttpHeaders.IF_MODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");

    response.getHeaders().add(HttpHeaders.ETAG, "123");
    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:58 CEST");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("200 OK", response.getStatus());
  }

  // if get, if modified since false -> 304
  @Test
  public void GetIfModifiedSinceFalse() {

    request.setMethod(HttpBase.Method.GET);
    request.getHeaders().add(HttpHeaders.IF_MODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");

    response.getHeaders().add(HttpHeaders.ETAG, "123");
    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:56 CEST");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }

  // if head, if modified since true -> 200
  @Test
  public void HeadIfModifiedSinceTrue() {

    request.setMethod(HttpBase.Method.HEAD);
    request.getHeaders().add(HttpHeaders.IF_MODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");

    response.getHeaders().add(HttpHeaders.ETAG, "123");
    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:58 CEST");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("200 OK", response.getStatus());
  }

  // if head, if modified since false -> 304
  @Test
  public void HeadIfModifiedSinceFalse() {

    request.setMethod(HttpBase.Method.HEAD);
    request.getHeaders().add(HttpHeaders.IF_MODIFIED_SINCE, "Thu, 29 Sep 2016 15:00:57 CEST");

    response.getHeaders().add(HttpHeaders.ETAG, "123");
    response.getHeaders().add(HttpHeaders.LAST_MODIFIED, "Thu, 29 Sep 2016 15:00:56 CEST");

    handler.handle(request, response);

    assertEquals("123", response.getHeaders().get(HttpHeaders.ETAG));

    assertEquals("304 Not modified", response.getStatus());
  }



}