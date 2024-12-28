package org.apache.ibatis.scripting;

public interface ExpressionEvaluator {
  boolean evaluateBoolean(String expression, Object parameterObject);

  Object getValue(String expression, Object root);

  Iterable<?> evaluateIterable(String expression, Object parameterObject, boolean nullable);
}
