package org.apache.ibatis.scripting.xmltags;

import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.StringJoiner;

public class DefaultDynamicContext implements DynamicContext {

  private final ContextMap bindings;
  private final StringJoiner sqlBuilder = new StringJoiner(" ");
  private int uniqueNumber;

  public DefaultDynamicContext(ContextMap bindings) {
    this.bindings = bindings;
  }

  @Override
  public @NotNull Map<String, Object> getBindings() {
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
  public @NotNull String getSql() {
    return sqlBuilder.toString().trim();
  }

  @Override
  public int getUniqueNumber() {
    return uniqueNumber++;
  }
}
