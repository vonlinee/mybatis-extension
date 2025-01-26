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
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.SqlBuildContext;
import org.apache.ibatis.session.Configuration;
import org.jetbrains.annotations.NotNull;

/**
 * the sql that contains ${} or dynamic sql xml tag (if, foreach. etc.)
 * <blockquote><pre>
 * <select id="xxx">
 *   select * from t where id = ${id}
 * </select>
 * </pre></blockquote><p>
 * or
 * <blockquote><pre>
 * <select id="xxx">
 *   select * from t
 *   <if test="expression">where id = #{id}</if>
 * </select>
 * </pre></blockquote>
 *
 * @author Clinton Begin
 */
public class DynamicSqlSource implements SqlSource {

  private final SqlNode rootSqlNode;

  public DynamicSqlSource(SqlNode rootSqlNode) {
    this.rootSqlNode = rootSqlNode;
  }

  @Override
  @NotNull
  public BoundSql getBoundSql(@NotNull Configuration config, Object parameterObject) {
    SqlBuildContext context = config.createSqlBuildContext(parameterObject);
    // calculate all dynamic content
    rootSqlNode.apply(context);

    String sql = context.getSql();
    // the sql left may contain ${xxx} or #{xxx}, so we have to parse again
    SqlSource sqlSource = SqlSourceBuilder.parse(config, sql, parameterObject, context.getBindings());
    BoundSql boundSql = sqlSource.getBoundSql(config, parameterObject);
    context.getBindings().iterateFor(boundSql::setAdditionalParameter);
    return boundSql;
  }
}
