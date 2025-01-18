package org.apache.ibatis.executor;

import org.apache.ibatis.executor.qml.JdbcOperation;

/**
 * Interface for managing a chain of JDBC handlers.
 * This interface defines methods to append, prepend, and handle JDBC operations
 * through a sequence of handlers.
 */
public interface JdbcHandlerChain {

  /**
   * Appends a new JDBC handler to the end of the chain.
   *
   * @param handler the JdbcHandler to be added to the chain
   * @return the updated JdbcHandlerChain with the new handler appended
   */
  JdbcHandlerChain append(JdbcHandler handler);

  /**
   * Prepends a new JDBC handler to the beginning of the chain.
   *
   * @param handler the JdbcHandler to be added to the chain
   * @return the updated JdbcHandlerChain with the new handler prepended
   */
  JdbcHandlerChain prepend(JdbcHandler handler);

  /**
   * Handles the specified JDBC operation by passing it through the chain of handlers.
   *
   * @param operation the JdbcOperation to be handled
   * @return the processed JdbcOperation after it has been handled by the chain
   */
  JdbcOperation handle(JdbcOperation operation);
}
