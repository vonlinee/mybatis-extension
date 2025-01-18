package org.apache.ibatis.executor.qml;

import org.apache.ibatis.executor.SqlStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * Interface for defining JDBC operations.
 * This interface provides methods to set properties, manage connections,
 * and handle SQL command execution.
 */
public interface JdbcOperation {

  /**
   * Gets the type of SQL command to be executed.
   *
   * @return the SqlCommandType indicating the type of command
   */
  @NotNull
  SqlCommandType getCommandType();

  /**
   * Sets the properties for the JDBC operation.
   *
   * @param properties the properties to be set
   */
  void setProperties(@NotNull Properties properties);

  /**
   * Sets the database connection to be used for the operation.
   *
   * @param connection the Connection to be set
   * @throws SQLException if a database access error occurs
   */
  void setConnection(@NotNull Connection connection) throws SQLException;

  /**
   * Sets the statement to be used for executing SQL commands.
   *
   * @param statement the Statement to be set
   * @throws SQLException if a database access error occurs
   */
  void setStatement(@NotNull Statement statement) throws SQLException;

  /**
   * Prepares and binds parameters to the provided statement.
   *
   * @param statement the Statement to be parameterized
   * @throws SQLException if a database access error occurs
   */
  void parameterize(@NotNull Statement statement) throws SQLException;

  /**
   * Retrieves the executable SQL command associated with this operation.
   *
   * @return an ExecutableSQL object representing the SQL command
   */
  @NotNull
  SqlStatement getExecutableSql();

  /**
   * Handles the result set obtained from executing a SQL query.
   * This method can be overridden to process the result set as needed.
   *
   * @param resultSet the ResultSet to be processed
   * @throws SQLException if a database access error occurs
   */
  void handleResultSet(@NotNull ResultSet resultSet) throws SQLException;

  /**
   * Closes the specified database connection.
   *
   * @param connection the Connection to be closed
   * @throws SQLException if a database access error occurs
   */
  void closeConnection(@NotNull Connection connection) throws SQLException;

  /**
   * Closes the specified statement.
   *
   * @param statement the Statement to be closed
   * @throws SQLException if a database access error occurs
   */
  void closeStatement(@NotNull Statement statement) throws SQLException;

  /**
   * Closes the specified result set.
   *
   * @param resultSet the ResultSet to be closed
   * @throws SQLException if a database access error occurs
   */
  void closeResultSet(@NotNull ResultSet resultSet) throws SQLException;
}
