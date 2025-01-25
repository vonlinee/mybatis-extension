package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.SqlBuildContext;
import org.apache.ibatis.util.StringUtils;

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
      SqlNode sqlNode = getChild(0);
      return sqlNode.apply(context);
    }
    BufferedSqlBuildContextDelegator childContext = new BufferedSqlBuildContextDelegator(context);
    boolean result = super.apply(childContext);
    String sql = childContext.getSql();
    context.appendSql("(");
    context.appendSql(removePrefix(sql));
    context.appendSql(")");
    return result;
  }

  private String removePrefix(String sql) {
    int i = StringUtils.indexOfIgnoreCase(sql, "and");
    if (i >= 0) {
      sql = sql.substring(i + 3);
    } else if ((i = StringUtils.indexOfIgnoreCase(sql, "or")) >= 0) {
      sql = sql.substring(i + 2);
    }
    return sql;
  }
}
