package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.SqlBuildContext;

/**
 * @author vonlinee
 * @since 2025-01-19 22:35
 **/
public class AndSqlNode implements SqlNode {

  private final String type;
  private final String column;
  private final String property;

  public AndSqlNode(String type, String column, String property) {
    this.type = type;
    this.column = column;
    this.property = property;
  }

  @Override
  public String getName() {
    return "and";
  }

  @Override
  public boolean isDynamic() {
    return false;
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    return false;
  }
}
