package org.apache.ibatis.jdbc;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JdbcUtils {

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
