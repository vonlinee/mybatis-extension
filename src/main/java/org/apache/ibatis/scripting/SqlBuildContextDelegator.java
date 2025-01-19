package org.apache.ibatis.scripting;

import org.jetbrains.annotations.NotNull;

public class SqlBuildContextDelegator implements SqlBuildContext {

  @NotNull
  private final SqlBuildContext delegator;

  public SqlBuildContextDelegator(@NotNull SqlBuildContext delegator) {
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
  public int nextUniqueNumber() {
    return delegator.nextUniqueNumber();
  }
}
