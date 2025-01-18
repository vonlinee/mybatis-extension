package org.apache.ibatis.scripting;

import org.jetbrains.annotations.NotNull;

public class SqlBuilderContextDelegator implements SqlBuilderContext {

  @NotNull
  private final SqlBuilderContext delegator;

  public SqlBuilderContextDelegator(@NotNull SqlBuilderContext delegator) {
    this.delegator = delegator;
  }

  @Override
  public void setDatabaseId(String databaseId) {
    delegator.setDatabaseId(databaseId);
  }

  @Override
  public String getDatabaseId() {
    return delegator.getDatabaseId();
  }

  @Override
  public BindingContext getBindings() {
    return delegator.getBindings();
  }

  @Override
  public void bind(String name, Object value) {
    delegator.bind(name, value);
  }

  @Override
  public void appendSql(String sql) {
    delegator.appendSql(sql);
  }

  @Override
  public String getSql() {
    return delegator.getSql();
  }

  @Override
  public int getUniqueNumber() {
    return delegator.getUniqueNumber();
  }
}
