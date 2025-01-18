package org.apache.ibatis.executor;

import org.apache.ibatis.executor.qml.JdbcOperation;

/**
 * Interface for handling JDBC operations.
 * This interface defines methods to check support for specific JDBC operations
 * and to handle those operations within a chain of handlers.
 */
public interface JdbcHandler {

  /**
   * Determines whether this handler supports the specified JDBC operation.
   *
   * @param operation the JdbcOperation to be checked for support
   * @return true if this handler supports the specified operation; false otherwise
   */
  boolean supports(JdbcOperation operation);

  /**
   * Handles the specified JDBC operation.
   * This method will be called to process the operation within the given chain.
   *
   * @param operation the JdbcOperation to be handled
   * @param chain     the chain of JdbcHandlers to be invoked after handling this operation
   */
  void handle(JdbcOperation operation, JdbcHandlerChain chain);
}
