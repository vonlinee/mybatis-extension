package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.scripting.SqlBuildContext;
import org.apache.ibatis.scripting.SqlBuildContextDelegator;
import org.jetbrains.annotations.NotNull;

/**
 * A buffered delegator for the {@link SqlBuildContext} that accumulates SQL statements
 * in a buffer instead of directly appending them to the underlying context.
 *
 * @author vonlinee
 * @since 2025-01-25 0:50
 **/
public class BufferedSqlBuildContextDelegator extends SqlBuildContextDelegator {

  private final StringBuilder sqlBuffer;

  /**
   * Constructs a new {@code BufferedSqlBuildContextDelegator} with the specified delegator.
   *
   * @param delegator the {@link SqlBuildContext} to delegate to
   */
  public BufferedSqlBuildContextDelegator(@NotNull SqlBuildContext delegator) {
    super(delegator);
    sqlBuffer = new StringBuilder();
  }

  /**
   * Appends SQL to the internal buffer instead of the delegator context.
   *
   * @param sql the SQL to append
   */
  @Override
  public void appendSql(String sql) {
    sqlBuffer.append(sql);
  }

  /**
   * Retrieves the accumulated SQL from the buffer.
   *
   * @return the accumulated SQL string
   */
  @Override
  public String getSql() {
    return sqlBuffer.toString();
  }
}
