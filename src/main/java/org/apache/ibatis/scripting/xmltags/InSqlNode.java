package org.apache.ibatis.scripting.xmltags;

public final class InSqlNode extends ForEachSqlNode {

  private final String column;

  @Override
  public String getName() {
    return "in";
  }

  public InSqlNode(SqlNode contents, String collectionExpression, Boolean nullable, String column) {
    super(contents, collectionExpression, nullable, null, null, "(", ")", ",");
    this.column = column;
  }
}
