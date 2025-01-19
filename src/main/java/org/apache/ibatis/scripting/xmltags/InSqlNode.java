package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.SqlBuildContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * used when write sql with syntax like t.column in (1, 2, 3).
 *
 * @author vonlinee
 * @see ForEachSqlNode
 */
public class InSqlNode extends ForEachSqlNode {

  /**
   * the colum used in sql syntax like t.column in (1, 2, 3).
   *
   * @apiNote this should a literal string, not a variable. e.g. this is optional.
   */
  @Nullable
  private final String column;

  /**
   * expression that evaluate to boolean value
   * if null, check the collection is not empty
   */
  @Nullable
  private final String condition;

  @Override
  public String getName() {
    return "in";
  }

  public InSqlNode(SqlNode contents, @NotNull ExpressionEvaluator evaluator,
                   @NotNull String collectionExpression,
                   @NotNull String conditionExpression,
                   @Nullable String itemExpression,
                   boolean nullable,
                   @Nullable String column) {
    super(contents, collectionExpression, nullable, "index", itemExpression, "(", ")", ",");
    this.condition = conditionExpression;
    this.evaluator = evaluator;
    this.column = column;
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    if (this.column != null) {
      context.appendSql(this.column);
    }
    context.appendSql(" in ");
    return super.apply(context);
  }
}
