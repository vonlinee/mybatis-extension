package org.apache.ibatis.executor;

import org.jetbrains.annotations.NotNull;

/**
 * Interface representing an executable SQL command.
 * This interface provides a method to retrieve the SQL command as a string.
 */
public interface SqlStatement {

  /**
   * Retrieves the SQL command as a String.
   *
   * @return the SQL command to be executed
   */
  @NotNull
  String getSql();
}
