package org.apache.ibatis.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Utility class for JDBC operations.
 * This class provides methods to establish database connections,
 * execute SQL queries, and close JDBC resources.
 */
public final class JdbcUtils {

  /**
   * Closes the specified Connection.
   *
   * @param conn the Connection to be closed
   */
  public static void closeSilently(Connection conn) {
    if (conn != null) {
      try {
        conn.close();
      } catch (SQLException ignore) {
      }
    }
  }

  /**
   * Closes the specified Statement.
   *
   * @param stmt the Statement to be closed
   */
  public static void closeSilently(Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException ignore) {
      }
    }
  }

  /**
   * Closes the specified ResultSet.
   *
   * @param rs the ResultSet to be closed
   */
  public static void closeSilently(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException ignore) {
      }
    }
  }
}
