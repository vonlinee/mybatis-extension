package org.apache.ibatis.scripting.xmltags;

public abstract class DynamicSqlNode implements SqlNode {

  @Override
  public final boolean isDynamic() {
    return true;
  }
}
