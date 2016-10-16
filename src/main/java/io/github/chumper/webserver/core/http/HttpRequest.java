package io.github.chumper.webserver.core.http;

/**
 * An {@link HttpRequest} object will contain all headers and content
 */
public class HttpRequest extends HttpBase {

  /**
   * The method of this request
   */
  private Method method;
  /**
   * The path this request asks for
   */
  private String path;
  /**
   * The content as string
   */
  private String content;

  public void setMethod(Method method) {
    this.method = method;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public Method getMethod() {
    return method;
  }

  public String getPath() {
    return path;
  }

  public String getContent() {
    return content;
  }

  public HttpHeaders getHeaders() {
    return headers;
  }

  public Version getProtocol() {
    return protocol;
  }

  public void setProtocol(Version protocol) {
    this.protocol = protocol;
  }

  /**
   * Will check whether this request should be survive more than one request reply cycle
   * @return boolean Whether to keep the underlying socket open or not
   */
  public boolean isKeepAlive() {
    if(getProtocol() == Version.HTTP_1_0 && headers.get(HttpHeaders.CONNECTION) == null) {
      return false;
    }
    if(getProtocol() == Version.HTTP_1_1 && headers.get(HttpHeaders.CONNECTION) == null) {
      return true;
    }
    if(headers.get(HttpHeaders.CONNECTION).equals("close")) {
      return false;
    }
    return true;
  }
}
