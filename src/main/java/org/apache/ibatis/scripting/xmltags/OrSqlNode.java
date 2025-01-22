package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.SqlBuildContext;

import java.util.List;

/**
 * @author vonlinee
 * @since 2025-01-19 22:35
 **/
public class OrSqlNode extends MixedSqlNode {

  private final String type;
  private final String column;
  private final String property;
  private final String test;

  public OrSqlNode(List<SqlNode> contents, String type, String column, String property, String test) {
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
  public boolean isDynamic() {
    return false;
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    return false;
  }
}
