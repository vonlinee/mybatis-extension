package org.apache.ibatis.jdbc;

import org.jetbrains.annotations.NotNull;

public interface Entity<T> {

  /**
   * the id
   *
   * @return id
   */
  @NotNull
  T getId();

  void setId(@NotNull T id);
}
