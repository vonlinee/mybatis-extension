package org.apache.ibatis.executor.qml;

import org.apache.ibatis.mapping.MappedStatement;

/**
 * @see MappedStatement
 */
public class MappedSqlOperation extends AbstractJdbcOperation {

  MappedStatement mappedStatement;
  Object parameterObject;
}
