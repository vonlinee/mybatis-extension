package org.apache.ibatis.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {

  /**
   * Apply a transaction timeout.
   * <p>
   * Update a query timeout to apply a transaction timeout.
   * </p>
   *
   * @param statement          a target statement
   * @param queryTimeout       a query timeout
   * @param transactionTimeout a transaction timeout
   * @throws SQLException if a database access error occurs, this method is called on a closed <code>Statement</code>
   */
  public static void applyTransactionTimeout(Statement statement, Integer queryTimeout, Integer transactionTimeout)
    throws SQLException {
    if (transactionTimeout == null) {
      return;
    }
    if (queryTimeout == null || queryTimeout == 0 || transactionTimeout < queryTimeout) {
      statement.setQueryTimeout(transactionTimeout);
    }
  }

  /**
   * Silently closes a ResultSet.
   *
   * @param rs the ResultSet to close; if null, no action is taken.
   */
  public static void closeSilently(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException ignore) {
        // Ignore exception
      }
    }
  }

  /**
   * Silently closes a Statement.
   *
   * @param stmt the Statement to close; if null, no action is taken.
   */
  public static void closeSilently(Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException ignore) {
        // Ignore exception
      }
    }
  }

  /**
   * Silently closes a Connection.
   *
   * @param conn the Connection to close; if null, no action is taken.
   */
  public static void closeSilently(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException ignore) {
        // Ignore exception
      }
    }
  }
}
