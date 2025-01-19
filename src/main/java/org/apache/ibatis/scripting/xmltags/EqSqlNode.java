package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.executor.NamingStrategy;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.SqlBuildContext;

/**
 * @author vonlinee
 * @since 2025-01-19 20:25
 **/
public class EqSqlNode extends DynamicSqlNode {

  private String column;

  private String property;

  private NamingStrategy namingStrategy;

  public EqSqlNode(String column, String property, NamingStrategy namingStrategy) {
    this.column = column;
    this.property = property;
    this.namingStrategy = namingStrategy;
  }

  @Override
  public String getName() {
    return "eq";
  }

  @Override
  public boolean apply(SqlBuildContext context) {

    String column = this.column;
    if (this.column == null) {


    }
    return false;
  }
}
