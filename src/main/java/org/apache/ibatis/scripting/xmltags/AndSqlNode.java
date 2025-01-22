package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.SqlBuildContext;

import java.util.List;

/**
 * @author vonlinee
 * @since 2025-01-19 22:35
 **/
public class AndSqlNode extends MixedSqlNode {

  private final String type;

  /**
   * column used in condition.
   */
  private final String column;

  /**
   * property
   */
  private final String property;

  /**
   * test condition expression.
   */
  private final String test;

  public AndSqlNode(List<SqlNode> contents, String type, String column, String property, String test) {
    super(contents);
    this.type = type;
    this.column = column;
    this.property = property;
    this.test = test;
  }

  @Override
  public String getName() {
    return "and";
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    if (getChildCount() == 1) {
      // it should be a StaticTextSqlNode
      context.appendSql(" AND ");
      SqlNode sqlNode = getContents().get(0);
      return sqlNode.apply(context);
    }
    context.appendSql(" AND (");
    boolean result = super.apply(context);
    context.appendSql(" )");

    return result;
  }
}
