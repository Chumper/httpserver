package io.github.chumper.webserver.util;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Will test if the simple logger replaces the placeholders correctly
 */
public class LoggerTest {

  private class TestLogger extends Logger {

    private String loggetMessage;

    @Override
    void doLog(String msg) {
      loggetMessage = msg;
    }
  }

  @Test
  public void testLogger() {
    TestLogger logger = new TestLogger();

    // We only test with contains, because the logger will add the timestamp which we can not
    // predict

    logger.log("Foobar {}", 1);
    assertTrue(logger.loggetMessage.contains("Foobar 1"));

    logger.log("Foobar {}{}{}", 1, 2);
    assertTrue(logger.loggetMessage.contains("Foobar 12{}"));

    logger.log("Foobar {}{}{}", 1, 2, 3, 4);
    assertTrue(logger.loggetMessage.contains("Foobar 123"));
  }

}