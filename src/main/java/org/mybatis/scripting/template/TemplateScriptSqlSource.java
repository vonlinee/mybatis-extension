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

import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.scripting.xmltags.DynamicContext;
import org.apache.ibatis.session.Configuration;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

public class TemplateScriptSqlSource implements SqlSource {

  public static final String MAPPING_COLLECTOR_KEY = "_pmc";
  protected static final String VARIABLES_KEY = "_vars";

  private final ParameterMapping[] parameterMappingSources;
  private final Template template;
  private final Configuration configuration;

  public TemplateScriptSqlSource(TemplateEngine engine, Configuration newConfiguration, String script, Class<?> parameterTypeClass) {
    this.configuration = newConfiguration;
    ParameterMappingSourceParser mappingParser = new ParameterMappingSourceParser(newConfiguration, script,
      parameterTypeClass);

    this.parameterMappingSources = mappingParser.getParameterMappingSources();
    this.template = engine.getTemplate(null, mappingParser.getSql());
  }

  @Override
  public BoundSql getBoundSql(Object parameterObject) {
    final Map<String, Object> context = new HashMap<>();
    final ParameterMappingCollector pmc = new ParameterMappingCollector(this.parameterMappingSources, context,
      this.configuration);

    context.put(DynamicContext.DATABASE_ID_KEY, this.configuration.getDatabaseId());
    context.put(DynamicContext.PARAMETER_OBJECT_KEY, parameterObject);
    context.put(MAPPING_COLLECTOR_KEY, pmc);
    context.put(VARIABLES_KEY, this.configuration.getVariables());
    StringWriter stringWriter = new StringWriter();
    this.template.render(context, stringWriter);
    final String sql = stringWriter.toString();
    BoundSql boundSql = new BoundSql(this.configuration, sql, pmc.getParameterMappings(), parameterObject);
    for (Map.Entry<String, Object> entry : context.entrySet()) {
      boundSql.setAdditionalParameter(entry.getKey(), entry.getValue());
    }

    return boundSql;
  }
}
