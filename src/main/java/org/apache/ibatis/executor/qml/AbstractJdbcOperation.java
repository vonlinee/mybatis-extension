package org.apache.ibatis.executor.qml;

import org.apache.ibatis.executor.SqlStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.util.JdbcUtils;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

public abstract class AbstractJdbcOperation implements JdbcOperation {

  protected Properties properties;
  protected Connection connection;
  protected Statement statement;
  protected SqlCommandType commandType;
  protected SqlStatement sql;

  @Override
  @NotNull
  public SqlCommandType getCommandType() {
    return commandType;
  }

  @Override
  public void setProperties(@NotNull Properties properties) {
    this.properties = properties;
  }

  @Override
  public void setConnection(@NotNull Connection connection) {
    this.connection = connection;
  }

  @Override
  public void setStatement(@NotNull Statement statement) {
    this.statement = statement;
  }

  @Override
  @NotNull
  public SqlStatement getExecutableSql() {
    return sql;
  }

  @Override
  public void handleResultSet(@NotNull ResultSet resultSet) {

  }

  @Override
  public void parameterize(@NotNull Statement statement) {
  }

  @Override
  public void closeConnection(@NotNull Connection connection) {
    JdbcUtils.closeSilently(connection);
  }

  @Override
  public void closeStatement(@NotNull Statement statement) {
    JdbcUtils.closeSilently(statement);
  }

  @Override
  public void closeResultSet(@NotNull ResultSet resultSet) {
    JdbcUtils.closeSilently(resultSet);
  }
}
