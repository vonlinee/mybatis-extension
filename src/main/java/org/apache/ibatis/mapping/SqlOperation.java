package org.apache.ibatis.mapping;

import java.util.List;

public interface SqlOperation {

  String getNativeSql();

  boolean isParameterized();

  List<ParameterMapping> getParameterMappings();

  Object getParameterObject();
}
