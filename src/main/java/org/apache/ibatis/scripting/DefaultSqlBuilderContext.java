package org.apache.ibatis.scripting;

import java.util.StringJoiner;

public class DefaultSqlBuilderContext implements SqlBuilderContext {

  private final BindingContext bindings;
  private final StringJoiner sqlBuilder = new StringJoiner(" ");
  private int uniqueNumber;
  private String databaseId;

  public DefaultSqlBuilderContext(BindingContext bindings) {
    this.bindings = bindings;
  }

  @Override
  public void setDatabaseId(String databaseId) {
    this.databaseId = databaseId;
  }

  @Override
  public String getDatabaseId() {
    return databaseId;
  }

  @Override
  public BindingContext getBindings() {
    return bindings;
  }

  @Override
  public void bind(String name, Object value) {
    bindings.put(name, value);
  }

  @Override
  public void appendSql(String sql) {
    sqlBuilder.add(sql);
  }

  @Override
  public String getSql() {
    return sqlBuilder.toString().trim();
  }

  @Override
  public int getUniqueNumber() {
    return uniqueNumber++;
  }
}
