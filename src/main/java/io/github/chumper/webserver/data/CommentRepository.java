package io.github.chumper.webserver.data;

import java.util.stream.Stream;

/**
 * The interface for the interaction of comments with the persistence layer
 */
public interface CommentRepository {

  /**
   * Will get all comments
   * @return A List of comments
   */
  Stream<Comment> all();

  /**
   * Add a comment
   * @param comment The comment to add
   */
  void add(Comment comment);

  /**
   * Will clear all comments
   */
  void clear();
}
