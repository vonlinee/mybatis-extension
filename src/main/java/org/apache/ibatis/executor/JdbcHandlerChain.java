package org.apache.ibatis.executor;

import org.apache.ibatis.executor.qml.JdbcOperation;

public interface JdbcHandlerChain {

  JdbcHandlerChain append(JdbcHandler handler);

  JdbcHandlerChain prepend(JdbcHandler handler);

  JdbcOperation handle(JdbcOperation operation);
}
