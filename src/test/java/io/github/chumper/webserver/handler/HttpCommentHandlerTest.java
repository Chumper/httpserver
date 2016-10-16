package io.github.chumper.webserver.handler;

import org.junit.Before;
import org.junit.Test;

import java.time.Instant;

import io.github.chumper.webserver.core.http.HttpBase;
import io.github.chumper.webserver.core.http.HttpHandler;
import io.github.chumper.webserver.core.http.HttpRequest;
import io.github.chumper.webserver.core.http.HttpResponse;
import io.github.chumper.webserver.data.Comment;
import io.github.chumper.webserver.data.CommentRepository;
import io.github.chumper.webserver.util.InMemoryRepo;

import static org.junit.Assert.*;

public class HttpCommentHandlerTest {

  private CommentRepository repo = new InMemoryRepo();
  private HttpHandler handler = new HttpCommentHandler(repo);

  private HttpResponse response;
  private HttpRequest request;

  @Before
  public void setUp() throws Exception {
    this.request = new HttpRequest();
    this.response = new HttpResponse();
    this.repo.clear();
  }

  /**
   * Will test if comments will be listed correctly
   */
  @Test
  public void listNoComments() {

    request.setPath("/comments");
    request.setMethod(HttpBase.Method.GET);

    handler.handle(request, response);

    assertTrue(response.getContent().toString().contains("<form method=\"post\">"));
  }

  /**
   * Will test if comments will be listed correctly
   */
  @Test
  public void listComments() {

    repo.add(new Comment(Instant.now(), "John", "Doe"));

    request.setPath("/comments");
    request.setMethod(HttpBase.Method.GET);

    handler.handle(request, response);

    assertTrue(response.getContent().toString().contains("<form method=\"post\">"));
    assertTrue(response.getContent().toString().contains("name: John"));
    assertTrue(response.getContent().toString().contains("message: Doe"));
  }

  @Test
  public void addComments() {

    request.setPath("/comments");
    request.setMethod(HttpBase.Method.POST);
    request.setContent("name=John&message=Doe&add=Submit");

    handler.handle(request, response);

    assertTrue(response.getContent().toString().contains("<form method=\"post\">"));
    assertTrue(response.getContent().toString().contains("name: John"));
    assertTrue(response.getContent().toString().contains("message: Doe"));

  }

  @Test
  public void deleteComments() {
    repo.add(new Comment(Instant.now(), "John", "Doe"));

    request.setPath("/comments");
    request.setMethod(HttpBase.Method.POST);
    request.setContent("name=John&message=Doe&clear=Submit");

    handler.handle(request, response);

    assertTrue(response.getContent().toString().contains("<form method=\"post\">"));
    assertFalse(response.getContent().toString().contains("name: John"));
    assertFalse(response.getContent().toString().contains("message: Doe"));
  }

}