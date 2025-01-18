package org.apache.ibatis.executor;

import org.apache.ibatis.executor.qml.JdbcOperation;

public interface JdbcHandler {

  boolean supports(JdbcOperation operation);

  void handle(JdbcOperation operation, JdbcHandlerChain chain);
}
