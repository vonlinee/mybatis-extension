package org.apache.ibatis.scripting;

public interface ExpressionEvaluator {

  boolean evaluateBoolean(String expression, Object parameterObject);

  Iterable<?> evaluateIterable(String expression, Object parameterObject, boolean nullable);

  Object getValue(String content, Object parameter);
}
