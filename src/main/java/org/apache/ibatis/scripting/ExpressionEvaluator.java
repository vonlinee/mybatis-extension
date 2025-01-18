package org.apache.ibatis.scripting;

/**
 * An interface for evaluating expressions against a parameter object.
 */
public interface ExpressionEvaluator {

  /**
   * Evaluates a boolean expression.
   *
   * @param expression      the expression to evaluate
   * @param parameterObject the object used as a parameter in the evaluation
   * @return true if the expression evaluates to true; false otherwise
   */
  boolean evaluateBoolean(String expression, Object parameterObject);

  /**
   * Evaluates an expression and returns an iterable of results.
   *
   * @param expression      the expression to evaluate
   * @param parameterObject the object used as a parameter in the evaluation
   * @param nullable        indicates whether the result can be null
   * @return an iterable containing the results of the evaluation
   */
  Iterable<?> evaluateIterable(String expression, Object parameterObject, boolean nullable);

  /**
   * Retrieves a value based on the provided content and parameter.
   *
   * @param content   the content to evaluate
   * @param parameter the parameter used in the evaluation
   * @return the evaluated value
   */
  Object getValue(String content, Object parameter);
}
