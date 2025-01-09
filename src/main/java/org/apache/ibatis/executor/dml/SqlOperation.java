package org.apache.ibatis.executor.dml;

import org.apache.ibatis.mapping.BoundSql;

public interface SqlOperation {

  BoundSql getSql();
}
