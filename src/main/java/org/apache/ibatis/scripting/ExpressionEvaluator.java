package org.apache.ibatis.scripting;

public interface ExpressionEvaluator {

  /**
   * 布尔表达式解析，对于返回值为数字的if表达式,0为假，非0为真
   *
   * @param expression      表达式
   * @param parameterObject 参数对象
   * @return true/false
   */
  boolean evaluateBoolean(String expression, Object parameterObject);

  Object getValue(String expression, Object root);

  Iterable<?> evaluateIterable(String expression, Object parameterObject, boolean nullable);
}
