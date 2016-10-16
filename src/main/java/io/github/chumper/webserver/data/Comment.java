package io.github.chumper.webserver.data;

import java.time.Instant;

/**
 * Entity class for comments, contains a date, a name and a message
 */
public class Comment {

  public final Instant posted;
  public final String name;
  public final String message;

  public Comment(Instant posted,
                 String name,
                 String message) {
    this.posted = posted;
    this.name = name;
    this.message = message;
  }

  @Override
  public String toString() {
    return   "{<br/>"
           + "  posted: " + posted.toString() + "<br/>"
           + "  name: " + name + "<br/>"
           + "  message: " + message + "<br/>"
           + "}<br/><br/>";
  }
}
