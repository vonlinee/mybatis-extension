/*
 *    Copyright 2009-2023 the original author or authors.
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

import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.scripting.BindingContext;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.util.StringUtils;
import org.jetbrains.annotations.Nullable;

/**
 * @author Clinton Begin
 */
public class SqlSourceBuilder {

  public static SqlSource parse(Configuration configuration, String originalSql, @Nullable Object parameterObject, BindingContext additionalParameters) {
    Class<?> parameterType = parameterObject == null ? Object.class : parameterObject.getClass();
    return parse(configuration, originalSql, parameterType, additionalParameters);
  }

  public static SqlSource parse(Configuration configuration, String originalSql, @Nullable Class<?> parameterType, BindingContext additionalParameters) {
    parameterType = parameterType == null ? Object.class : parameterType;
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType,
      additionalParameters);
    GenericTokenParser parser = GenericTokenParser.ofSign(handler);
    String sql;
    if (configuration.isShrinkWhitespacesInSql()) {
      sql = parser.parse(StringUtils.removeExtraWhitespaces(originalSql));
    } else {
      sql = parser.parse(originalSql);
    }
    return new StaticSqlSource(sql, handler.getParameterMappings());
  }
}
