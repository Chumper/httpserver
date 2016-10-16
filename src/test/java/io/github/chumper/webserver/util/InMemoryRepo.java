package io.github.chumper.webserver.util;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import io.github.chumper.webserver.data.Comment;
import io.github.chumper.webserver.data.CommentRepository;

public class InMemoryRepo implements CommentRepository {

  List<Comment> comments = new ArrayList<>();

  @Override
  public Stream<Comment> all() {
    return comments.stream();
  }

  @Override
  public void add(Comment comment) {
    comments.add(comment);
  }

  @Override
  public void clear() {
    comments.clear();
  }
}
