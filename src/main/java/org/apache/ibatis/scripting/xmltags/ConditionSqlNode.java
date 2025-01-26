package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.NamedPredicate;
import org.apache.ibatis.scripting.PredicateFactory;
import org.apache.ibatis.scripting.SqlBuildContext;
import org.apache.ibatis.util.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author vonlinee
 * @apiNote
 * @since 2025-01-25 14:08
 **/
abstract class ConditionSqlNode extends MixedSqlNode {

  /**
   * test condition expression. it can be a valid ognl expression
   * that should be evaluated to be a boolean result, or a name of a predicate
   * registered in a NamedPredicateFactory
   *
   * @see NamedPredicate
   */
  @Nullable
  private final String test;

  /**
   * the text
   */
  @Nullable
  private final String text;

  /**
   * the variable in condition
   */
  @Nullable
  private final String variable;

  /**
   * predicate factory used when test is a reference to a named predicate
   *
   * @see NamedPredicate
   */
  private final PredicateFactory predicateFactory;

  /**
   * expression evaluator
   */
  private final ExpressionEvaluator evaluator;

  public ConditionSqlNode(@NotNull List<SqlNode> contents,
                          @Nullable String text,
                          @Nullable String test,
                          @NotNull PredicateFactory predicateFactory,
                          @NotNull ExpressionEvaluator evaluator) {
    super(contents);
    if (StringUtils.isBlank(test) && text != null) {
      List<String> variableNames = GenericTokenParser.collectVariableNames(text);
      if (variableNames.size() == 1) {
        this.variable = variableNames.get(0);
      } else {
        this.variable = null;
      }
    } else {
      this.variable = null;
    }

    this.test = test;
    this.text = text;
    this.predicateFactory = predicateFactory;
    this.evaluator = evaluator;
  }

  protected void appendConditionConnector(SqlBuildContext context) {
    context.appendSql(" ");
    context.appendSql(getName());
    context.appendSql(" ");
  }

  protected void appendNestedCondition(SqlBuildContext context, String condition) {
    context.appendSql("(");
    context.appendSql(condition);
    context.appendSql(")");
  }

  protected String removePrefix(String sql) {
    int i = StringUtils.indexOfIgnoreCase(sql, "and");
    if (i >= 0) {
      sql = sql.substring(i + 3);
    } else if ((i = StringUtils.indexOfIgnoreCase(sql, "or")) >= 0) {
      sql = sql.substring(i + 2);
    }
    return sql;
  }

  @Override
  public final boolean isDynamic() {
    return true;
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    if (getChildCount() == 1) {
      return applyCondition(context);
    }
    appendConditionConnector(context);
    return applyNestedCondition(context);
  }

  /**
   * @param context sql build context
   */
  protected boolean applyCondition(SqlBuildContext context) {
    String testCondition = this.test;
    if (testCondition == null) {
      if (this.variable != null) {
        Object value = evaluator.getValue(this.variable, context.getBindings());
        if (evaluate(value)) {
          appendConditionConnector(context);
          super.apply(context);
          return true;
        }
      } else if (text != null) {
        appendConditionConnector(context);
        context.appendSql(text);
        return true;
      }
    } else if (testCondition.startsWith("@")) {
      testCondition = testCondition.substring(1);
      NamedPredicate predicate = predicateFactory.get(testCondition, null);
      final Object value = evaluator.getValue(this.variable, context.getBindings());
      if (predicate != null && predicate.test(value)) {
        appendConditionConnector(context);
        super.apply(context);
        return true;
      }
    }
    return false;
  }

  protected boolean evaluate(Object value) {
    if (value instanceof Number) {
      return true;
    } else if (value instanceof String) {
      return StringUtils.isBlank((CharSequence) value);
    }
    return false;
  }

  /**
   * handle nested condition
   *
   * @param context context
   */
  protected boolean applyNestedCondition(SqlBuildContext context) {
    BufferedSqlBuildContextDelegator childContext = new BufferedSqlBuildContextDelegator(context);
    boolean result = super.apply(childContext);
    String sql = childContext.getSql();
    appendNestedCondition(context, removePrefix(sql));
    return result;
  }
}
