package io.github.chumper.webserver.core.http;

/**
 * Base for http request and response classes
 */
public abstract class HttpBase {

  public enum Method { GET, HEAD, POST }
  public enum Version { HTTP_1_0("HTTP/1.0"), HTTP_1_1("HTTP/1.1");

    private final String version;

    Version(String value) {
      this.version = value;
    }

    @Override
    public String toString() {
      return version;
    }
  }

  /**
   * The header of this requests
   */
  HttpHeaders headers = new HttpHeaders();

  /**
   * The protocol of the request
   */
  Version protocol = Version.HTTP_1_1;

  public Version getProtocol() {
    return protocol;
  }

  public void setProtocol(Version protocol) {
    this.protocol = protocol;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }
}
