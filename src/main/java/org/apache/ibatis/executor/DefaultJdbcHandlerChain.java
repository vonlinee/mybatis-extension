package org.apache.ibatis.executor;

import org.apache.ibatis.executor.qml.JdbcOperation;

import java.util.ArrayList;
import java.util.List;

public class DefaultJdbcHandlerChain implements JdbcHandlerChain {

  List<JdbcHandler> handlers = new ArrayList<>();

  @Override
  public JdbcHandlerChain append(JdbcHandler handler) {
    handlers.add(handler);
    return this;
  }

  @Override
  public JdbcHandlerChain prepend(JdbcHandler handler) {
    handlers.add(0, handler);
    return this;
  }

  @Override
  public JdbcOperation handle(JdbcOperation operation) {
    for (JdbcHandler handler : handlers) {
      if (handler.supports(operation)) {
        handler.handle(operation, this);
      }
    }
    return operation;
  }
}
