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
package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.parameter.ParameterHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.DynamicCheckerTokenParser;
import org.apache.ibatis.parsing.PropertyParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.LanguageDriver;
import org.apache.ibatis.scripting.MapBinding;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.ognl.OgnlExpressionEvaluator;
import org.apache.ibatis.session.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eduardo Macarron
 */
public class XMLLanguageDriver implements LanguageDriver {

  ExpressionEvaluator evaluator = new OgnlExpressionEvaluator();

  @Override
  public ParameterHandler createParameterHandler(MappedStatement mappedStatement, Object parameterObject,
                                                 BoundSql boundSql) {
    return new DefaultParameterHandler(mappedStatement, parameterObject, boundSql);
  }

  @Override
  public SqlSource createSqlSource(Configuration configuration, XNode script, Class<?> parameterType) {
    XMLScriptBuilder builder = new XMLScriptBuilder(configuration, parameterType);
    return builder.parseScriptNode(script);
  }

  /**
   * for example:
   * <blockquote><pre>
   * {@code @}Update({"<script>",
   *        "update Author",
   *        "  <set>",
   *        "    <if test='username != null'>username=#{username},</if>",
   *        "    <if test='password != null'>password=#{password},</if>",
   *        "    <if test='email != null'>email=#{email},</if>",
   *        "    <if test='bio != null'>bio=#{bio}</if>",
   *        "  </set>",
   *        "where id=#{id}",
   *        "</script>"})
   * void updateAuthorValues(Author author);
   * </pre></blockquote>
   *
   * @param configuration The MyBatis configuration
   * @param script        The content of the annotation
   * @param parameterType input parameter type got from a mapper method or specified in the parameterType xml attribute. Can be
   *                      null.
   * @return SqlSource
   */
  @Override
  public SqlSource createSqlSource(Configuration configuration, String script, Class<?> parameterType) {
    // issue #3
    if (script.startsWith("<script>")) {
      XPathParser parser = new XPathParser(script, false, configuration.getVariables(), new XMLMapperEntityResolver());
      return createSqlSource(configuration, parser.evalNode("/script"), parameterType);
    }
    // issue #127
    script = PropertyParser.parse(script, configuration.getVariables());
    if (DynamicCheckerTokenParser.isDynamic(script)) {
      return new DynamicSqlSource(new TextSqlNode(evaluator, script));
    }
    return new RawSqlSource(SqlSourceBuilder.parse(configuration, script, parameterType, new MapBinding()));
  }
}
