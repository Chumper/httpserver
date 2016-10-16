package io.github.chumper.webserver.util;

import java.time.Instant;

/**
 * Simple implementation that logs the given string with replacements
 */
public abstract class Logger {

  private final static String DELIMITER = "{}";

  /**
   * Takes a msg and given parameters and will log the formatted msg to the console.
   * The string can have variables placeholder with {} that will then be replaced one after another
   * with the given arguments.
   * @param msg The unedited String
   * @param args The arguments to inster into the message
   */
  public void log(String msg, Object... args) {
    StringBuilder builder = new StringBuilder(Instant.now().toString());
    builder.append(" - ");
    builder.append(msg);

    for (Object arg : args) {
      int index = builder.indexOf(DELIMITER);
      if (index != -1) {
        builder.replace(index, index + 2, String.valueOf(arg));
      }
    }
    doLog(builder.toString());
  }

  abstract void doLog(String msg);
}
