package org.apache.ibatis.scripting;

import java.util.Objects;

/**
 * @author vonlinee
 * @since 2025-01-19 15:36
 **/
public class SqlBuildContextWrapper implements SqlBuildContext {

  private SqlBuildContext context;

  public final void setWrapperContext(SqlBuildContext context) {
    this.context = Objects.requireNonNull(context, "the context wrapped cannot be null.");
  }

  public final SqlBuildContext getWrappedContext() {
    return context;
  }

  @Override
  public void setDatabaseId(String databaseId) {
    context.setDatabaseId(databaseId);
  }

  @Override
  public String getDatabaseId() {
    return context.getDatabaseId();
  }

  @Override
  public BindingContext getBindings() {
    return context.getBindings();
  }

  @Override
  public void bind(String name, Object value) {
    context.bind(name, value);
  }

  @Override
  public void appendSql(String sql) {
    context.appendSql(sql);
  }

  @Override
  public String getSql() {
    return context.getSql();
  }

  @Override
  public int getUniqueNumber() {
    return context.getUniqueNumber();
  }
}
