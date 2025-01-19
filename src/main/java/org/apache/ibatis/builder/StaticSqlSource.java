/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.session.Configuration;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * the sql that contains ? placeholder only
 * <blockquote><pre>
 * <select id="xxx">
 *   select * from t where id = #{id}
 * </select>
 * </pre></blockquote><p>
 *
 * @author Clinton Begin
 */
public class StaticSqlSource implements SqlSource {

  private final String sql;
  private final List<ParameterMapping> parameterMappings;

  public StaticSqlSource(String sql) {
    this(sql, null);
  }

  public StaticSqlSource(String sql, List<ParameterMapping> parameterMappings) {
    this.sql = sql;
    this.parameterMappings = parameterMappings;
  }

  @Override
  public @NotNull BoundSql getBoundSql(@NotNull Configuration config, Object parameterObject) {
    return new BoundSql(config, sql, parameterMappings, parameterObject);
  }

}
