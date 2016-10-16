package io.github.chumper.webserver.handler;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.chumper.webserver.core.http.HttpBase;
import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpHeaders;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;
import io.github.chumper.webserver.data.Comment;
import io.github.chumper.webserver.data.CommentRepository;
import io.github.chumper.webserver.data.MongoDbCommentRepository;
import io.github.chumper.webserver.util.ConsoleLogger;
import io.github.chumper.webserver.util.Logger;

/**
 * This handler will list all files from a directory if the requested path is a directory
 */
public class HttpCommentHandler
    implements HttpHandler {

  private static final Logger logger = new ConsoleLogger();

  private final CommentRepository repo;

  public HttpCommentHandler(CommentRepository repo) {
    this.repo = repo;
  }

  @Override
  public void handle(HttpRequest request,
                     HttpResponse response) {

    if (request.getPath().startsWith("/comments")) {
      response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "text/html");
      request.setPath(request.getPath().substring(9));
      // comment endpoint called

      // add comment
      if (request.getMethod() == HttpBase.Method.POST) {
        // decode params
        Map<String, String> params = splitQuery(request.getContent());
        if(params.get("add") != null) {
          Comment newComment = new Comment(Instant.now(), params.get("name"), params.get("message"));
          repo.add(newComment);
        } else {
          repo.clear();
        }
      }

      if (request.getMethod() == HttpBase.Method.GET ||
          request.getMethod() == HttpBase.Method.HEAD ||
          request.getMethod() == HttpBase.Method.POST) {
        // list comments
        repo.all().forEach(comment -> response.write(comment.toString()));
        response.write(getForm());
      }
    }
  }

  private String getForm() {
    return "<form method=\"post\">\n"
           + "  <input type=\"text\" name=\"name\" value=\"Name\"><br><br>\n"
           + "  <textarea name=\"message\">Message</textarea><br><br>\n"
           + "  <input type=\"submit\" name=\"add\" value=\"Submit\">\n"
           + "  <input type=\"submit\" name=\"clear\" value=\"Clear\">\n"
           + "</form>";
  }

  /**
   * TAKEN FROM http://stackoverflow.com/questions/13592236/parse-a-uri-string-into-name-value-collection
   */
  public static Map<String, String> splitQuery(String query) {
    Map<String, String> query_pairs = new LinkedHashMap<>();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      try {
        query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"),
                        URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
      } catch (UnsupportedEncodingException e) {
        logger.log("could not parse request params: {}", e.getMessage());
      }
    }
    return query_pairs;
  }
}
