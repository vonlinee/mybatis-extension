package org.apache.ibatis.mapping;

import java.util.List;

public interface SqlOperation {

  String getNativeSql();

  boolean isParameterized();

  List<ParameterMapping> getParameterMappings();

  Object getParameterObject();

  boolean hasAdditionalParameter(String name);

  void setAdditionalParameter(String name, Object value);

  Object getAdditionalParameter(String name);
}
