package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.ExpressionEvaluator;

public abstract class DynamicSqlNode implements SqlNode {

  protected ExpressionEvaluator evaluator;

  public void setExpressionEvaluator(ExpressionEvaluator evaluator) {
    this.evaluator = evaluator;
  }

  @Override
  public final boolean isDynamic() {
    return true;
  }
}
