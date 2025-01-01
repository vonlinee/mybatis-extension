package org.apache.ibatis.executor;

import org.apache.ibatis.mapping.SqlOperation;

public abstract class AbstractSqlOperation implements SqlOperation {

  String nativeSql;

  @Override
  public String getNativeSql() {
    return nativeSql;
  }
}
