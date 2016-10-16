package io.github.chumper.webserver.core.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Represents the response that will be returned to the client
 */
public class HttpResponse extends HttpBase {

  /**
   * The status of the response
   */
  private String status = "200 OK";
  /**
   * The cntent as byte array
   */
  private ByteArrayOutputStream content = new ByteArrayOutputStream();

  public void setStatus(String status) {
    this.status = status;
  }

  public String status() {
    return this.status;
  }

  public void write(String content){
    if (content == null) { return; }
    try {
      this.content.write(content.getBytes());
    } catch (IOException ignored) {
    }
  }

  public void write(byte[] bytes) throws IOException {
    if (bytes == null) { return; }
    this.content.write(bytes);
  }

  public String getStatus() {
    return status;
  }

  public int getContentLength() {
    return content.size();
  }

  public ByteArrayOutputStream getContent() {
    return content;
  }
}
