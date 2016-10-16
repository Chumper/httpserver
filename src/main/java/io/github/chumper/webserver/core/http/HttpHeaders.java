package io.github.chumper.webserver.core.http;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class HttpHeaders {

  public static final String CONNECTION = "Connection";
  public static final String CONTENT_TYPE = "Content-Type";
  public static final String CONTENT_LENGTH = "Content-Length";
  public static final String LAST_MODIFIED = "Last-Modified";
  public static final String IF_MATCH = "If-Match";
  public static final String IF_NONE_MATCH = "If-None-Match";
  public static final String IF_MODIFIED_SINCE = "If-Modified-Since";
  public static final String IF_UNMODIFIED_SINCE = "If-Unmodified-Since";
  public static final String ETAG = "ETag";

  public static final SimpleDateFormat headerTime = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

  private static final String HEADER_DELIMITER = ":";

  private final Map<String, String> headers = new HashMap<>();

  public void add(String headerLine) {

    String[] splittedHeader = headerLine.split(HEADER_DELIMITER, 2);
    if (splittedHeader.length == 2) {
      headers.put(splittedHeader[0].trim().substring(0, 1).toUpperCase() +
                  splittedHeader[0].trim().substring(1), splittedHeader[1].trim());
    }
  }

  public String get(String name) {
    if (name == null) { return null; }

    return headers.get(name);
  }

  public Set<Map.Entry<String, String>> get() {
    return headers.entrySet();
  }

  public void add(String key,
                  String value) {
    if (key == null) { return; }

    headers.put(key.trim(), value.trim());
  }

}
