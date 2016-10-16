package io.github.chumper.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import io.github.chumper.webserver.core.http.HttpBase;
import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpHeaders;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;

/**
 * Will check the request for Etag headers according to:
 * https://tools.ietf.org/html/draft-ietf-httpbis-p4-conditional-26#section-6
 * and
 * https://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html#sec14.24
 */
public class HttpETagHandler
    implements HttpHandler {

  @Override
  public void handle(HttpRequest request,
                     HttpResponse response) {

    if(response.getHeaders().get(HttpHeaders.ETAG) != null) {
      String etag = response.getHeaders().get(HttpHeaders.ETAG);

      step1(request, response, etag);
    }
  }

  private void step1(HttpRequest request,
                     HttpResponse response,
                     String etag) {
    if(request.getHeaders().get(HttpHeaders.IF_MATCH) != null) {
      if(doesMatch(request.getHeaders().get(HttpHeaders.IF_MATCH), etag)) {
        step3(request, response, etag);
      } else {
        response.setStatus("412 Precondition failed");
      }
    } else {
      step2(request, response, etag);
    }
  }

  private void step2(HttpRequest request,
                     HttpResponse response,
                     String etag) {
    if(request.getHeaders().get(HttpHeaders.IF_UNMODIFIED_SINCE) != null) {
      try {
        Date date1 = HttpHeaders.headerTime.parse(request.getHeaders().get(HttpHeaders.IF_UNMODIFIED_SINCE));
        Date date2 = HttpHeaders.headerTime.parse(response.getHeaders().get(HttpHeaders.LAST_MODIFIED));

        if(date2.before(date1)) {
          step3(request, response, etag);
        } else {
          response.setStatus("412 Precondition failed");
        }
      } catch (ParseException e) {
        // ignore this step as we can not parse the time
        step3(request, response, etag);
      }
    } else {
      step3(request, response, etag);
    }

  }

  private void step3(HttpRequest request,
                     HttpResponse response,
                     String etag) {

    if(request.getHeaders().get(HttpHeaders.IF_NONE_MATCH) != null) {
      if(!doesMatch(request.getHeaders().get(HttpHeaders.IF_NONE_MATCH), etag)) {
        step5(request, response, etag);
      } else if(request.getMethod() == HttpBase.Method.GET || request.getMethod() == HttpBase.Method.HEAD){
        response.setStatus("304 Not modified");
      } else {
        response.setStatus("412 Precondition failed");
      }
    } else {
     step4(request, response, etag);
    }

  }

  private void step4(HttpRequest request,
                     HttpResponse response,
                     String etag) {
    if((request.getMethod() == HttpBase.Method.GET || request.getMethod() == HttpBase.Method.HEAD) &&
        request.getHeaders().get(HttpHeaders.IF_MODIFIED_SINCE) != null){

      try {
        Date date1 = HttpHeaders.headerTime.parse(request.getHeaders().get(HttpHeaders.IF_MODIFIED_SINCE));
        Date date2 = HttpHeaders.headerTime.parse(response.getHeaders().get(HttpHeaders.LAST_MODIFIED));

        if(date2.after(date1)) {
          step5(request, response, etag);
        } else {
          response.setStatus("304 Not modified");
        }
      } catch (ParseException e) {
        // ignore this header
        step5(request, response, etag);
      }
    }
  }

  private void step5(HttpRequest request,
                     HttpResponse response,
                     String etag) {
    // we do not support if-range and range, so do nothing here
  }

  private boolean doesMatch(String value, String etag) {
    // check wild card
    if(value.replace("\"", "").equals("*")) {
      return true;
    }
    // split on ,
    for(String s : value.split(",")) {
      if(s.replace("\"", "").trim().equals(etag)) {
        return true;
      }
    }
    return false;
  }
}
