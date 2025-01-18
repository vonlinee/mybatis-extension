package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.SqlBuildContext;
import org.jetbrains.annotations.NotNull;

public final class InSqlNode extends ForEachSqlNode {

  private final String column;

  @Override
  public String getName() {
    return "in";
  }

  public InSqlNode(SqlNode contents, String collectionExpression, @NotNull Boolean nullable, String column) {
    super(contents, collectionExpression, nullable, null, null, "(", ")", ",");
    this.column = column;
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    return super.apply(context);
  }
}
