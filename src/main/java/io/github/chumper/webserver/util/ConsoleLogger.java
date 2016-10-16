package io.github.chumper.webserver.util;

/**
 * A Logger that will log the msg to the console
 */
public class ConsoleLogger extends Logger {

  @Override
  void doLog(String msg) {
    System.out.println(msg);
  }
}
