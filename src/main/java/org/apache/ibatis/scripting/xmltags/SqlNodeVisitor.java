package org.apache.ibatis.scripting.xmltags;

public interface SqlNodeVisitor {

  void appendSql(String sql);
}
