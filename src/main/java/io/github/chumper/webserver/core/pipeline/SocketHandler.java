package io.github.chumper.webserver.core.pipeline;

import java.net.Socket;

/**
 * Abstract Handler class that can be used to modify the pipeline of the server
 */
public interface SocketHandler {

  /**
   * Each {@link java.util.logging.SocketHandler} can return one of the following states. When a handler
   * returns DISCARD then the socket will be closed and no other handler will be called.
   */
  enum State { CONTINUE, DISCARD }

  /**
   * Will process the incoming msg and transform it to the outgoing object.
   * For the sake of this exercise we will discard the msg when null is returned.
   * A more sophisticated exercise should handle this in a better way.
   * @param socket The socket where the msg came from originally
   */
  State process(Socket socket);
}
