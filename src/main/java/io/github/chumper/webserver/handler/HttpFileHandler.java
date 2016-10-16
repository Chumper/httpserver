package io.github.chumper.webserver.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

import io.github.chumper.webserver.core.http.HttpBase;
import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpHeaders;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;
import io.github.chumper.webserver.util.ConsoleLogger;
import io.github.chumper.webserver.util.Logger;

/**
 * This handler will list all files from a directory if the requested path is a directory
 */
public class HttpFileHandler
    implements HttpHandler {

  private static final Logger logger = new ConsoleLogger();

  private final File root;

  public HttpFileHandler(String root) {
    this.root = new File(root);
  }

  @Override
  public void handle(HttpRequest request,
                     HttpResponse response) {

    if (request.getMethod() == HttpBase.Method.GET || request.getMethod() == HttpBase.Method.HEAD) {
      if (request.getPath().startsWith("/files")) {
        request.setPath(request.getPath().substring(6));
        File file = new File(root.getAbsolutePath() + request.getPath());

        //download file
        if (file.exists() && file.isFile()) {
          // download
          try {
            response.write(Files.readAllBytes(file.toPath()));
          } catch (IOException e) {
            logger.log("Error during file read: {}", e.getMessage());
            response.setStatus("500 Internal server error");
            return;
          }

          response.getHeaders().add(HttpHeaders.ETAG, String.valueOf(file.lastModified()));
          response.getHeaders()
              .add(HttpHeaders.LAST_MODIFIED, HttpHeaders.headerTime.format(file.lastModified()));
        }

        // list files
        if (file.exists() && file.isDirectory()) {
          // list files
          Long lastModified = 0l;
          StringBuilder builder = new StringBuilder();
          for (File f : file.listFiles(it -> it.isFile() || it.isDirectory())) {
            builder.append(root.toPath().toAbsolutePath().relativize(f.toPath())).append("\r\n");
            if (f.lastModified() > lastModified) {
              lastModified = f.lastModified();
            }
          }

          String content = builder.toString();

          response.getHeaders().add(HttpHeaders.ETAG, String.valueOf(Math.abs(content.hashCode())));
          response.getHeaders()
              .add(HttpHeaders.LAST_MODIFIED, HttpHeaders.headerTime.format(lastModified));

          response.write(content);

        } else if (!file.exists()) {
          response.setStatus("404 Not found");
        }
      }
    }
  }
}
