package io.github.chumper.webserver;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;

import io.github.chumper.webserver.core.HttpServer;
import io.github.chumper.webserver.data.MongoDbCommentRepository;
import io.github.chumper.webserver.handler.HttpCommentHandler;
import io.github.chumper.webserver.handler.HttpETagHandler;
import io.github.chumper.webserver.handler.HttpFileHandler;
import io.github.chumper.webserver.handler.HttpKeepAliveHandler;
import io.github.chumper.webserver.handler.HttpRequestLogHandler;
import io.github.chumper.webserver.handler.HttpRootHandler;
import io.github.chumper.webserver.util.ConsoleLogger;
import io.github.chumper.webserver.util.Logger;

/**
 * Main bootstrap class used to parse all configurations, to configure and start the server
 */
public class Main {

  private static final Logger logger = new ConsoleLogger();

  public static void main(String... args)
      throws ExecutionException, InterruptedException, IOException {

    // Load the configuration from the environment as well as the arguments
    Config config = ConfigFactory.load();

    // create an http server
    HttpServer server = new HttpServer(
        config.getInt("server.port"),
        config.getInt("server.threads")
    );

    // add handler that will work on /
    server.addHttpHandler(new HttpRootHandler());

    // if comments are active, add handler for that and give it a mongo repository
    if(config.getBoolean("server.comments.active")) {
      server.addHttpHandler(new HttpCommentHandler(
          new MongoDbCommentRepository(
            config.getString("server.comments.host"),
            config.getInt("server.comments.port")
          )
      ));
    }

    // if files are active add the handler and the etag handling
    if(config.getBoolean("server.files.active")) {
      server.addHttpHandler(new HttpFileHandler(config.getString("server.files.root")));
      server.addHttpHandler(new HttpETagHandler());
    }

    // add the keep alive handler so sockets can be reused
    server.addHttpHandler(new HttpKeepAliveHandler());

    // add logging handler if active
    if(config.getBoolean("server.logging.active")) {
      server.addHttpHandler(new HttpRequestLogHandler());
    }

    // start the server and wait until it is started
    server.start().get();

    logger.log("Press RETURN to stop the server");

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    br.readLine();

    // Stop the server
    server.stop();
  }
}
