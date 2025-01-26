package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.PredicateFactory;

import java.util.List;

/**
 * @author vonlinee
 * @since 2025-01-19 22:35
 **/
public class OrSqlNode extends ConditionSqlNode {

  public OrSqlNode(List<SqlNode> contents, ExpressionEvaluator evaluator,
                   PredicateFactory predicateFactory, String text, String test) {
    super(contents, text, test, predicateFactory, evaluator);
  }

  @Override
  public String getName() {
    return "OR";
  }
}
