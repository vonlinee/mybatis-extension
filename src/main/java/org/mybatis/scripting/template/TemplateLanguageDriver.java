/*
 *    Copyright 2012-2022 the original author or authors.
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
package org.mybatis.scripting.template;

import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.mybatis.scripting.template.velocity.VelocityTemplateEngine;

/**
 * The {@link LanguageDriver} using template language.
 */
public class TemplateLanguageDriver implements LanguageDriver {

  private final TemplateEngine engine = new VelocityTemplateEngine();

  @Override
  public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject,
                                                 BoundSql boundSql) {
    return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
  }

  @Override
  public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterTypeClass) {
    return new TemplateScriptSqlSource(this.engine, configuration, script.getNode().getTextContent(),
      parameterTypeClass == null ? Object.class : parameterTypeClass);
  }

  @Override
  public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterTypeClass) {
    return new TemplateScriptSqlSource(this.engine, configuration, script, parameterTypeClass == null ? Object.class : parameterTypeClass);
  }
}
