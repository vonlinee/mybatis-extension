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

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.ParameterExpression;
import org.apache.ibatis.internal.StringKey;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

import java.util.ArrayList;
import java.util.List;

/**
 * valid properties below:
 * javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName
 */
public class ParameterMappingSourceParser {

  private final String sql;

  private final ParameterMapping[] parameterMappingSources;

  public ParameterMappingSourceParser(Configuration configuration, String script, Class<?> parameterType) {
    ParameterMappingTokenHandler handler = new ParameterMappingTokenHandler(configuration, parameterType);
    GenericTokenParser parser = new GenericTokenParser("@{", "}", handler);
    this.sql = parser.parse(script);
    this.parameterMappingSources = handler.getParameterMappingSources();
  }

  public ParameterMapping[] getParameterMappingSources() {
    return this.parameterMappingSources;
  }

  public String getSql() {
    return this.sql;
  }

  private static class ParameterMappingTokenHandler extends BaseBuilder implements TokenHandler {

    private final List<ParameterMapping> parameterMappings = new ArrayList<>();
    private final Class<?> parameterType;

    public ParameterMappingTokenHandler(Configuration newConfiguration, Class<?> newParameterType) {
      super(newConfiguration);
      this.parameterType = newParameterType;
    }

    public ParameterMapping[] getParameterMappingSources() {
      return this.parameterMappings.toArray(new ParameterMapping[0]);
    }

    @Override
    public String handleToken(String content) {
      int index = this.parameterMappings.size();
      ParameterMapping pm = buildParameterMapping(content);
      this.parameterMappings.add(pm);
      return '$' + TemplateScriptSqlSource.MAPPING_COLLECTOR_KEY + ".g(" + index +
        ")";
    }

    private ParameterMapping buildParameterMapping(String content) {
      ParameterExpression expression = parseParameterMapping(content);
      String property = expression.getProperty();
      final String jdbcType = expression.getJdbcType();
      Class<?> propertyType;
      if (this.configuration.hasTypeHandler(this.parameterType)) {
        propertyType = this.parameterType;
      } else if (JdbcType.CURSOR.name().equals(jdbcType)) {
        propertyType = java.sql.ResultSet.class;
      } else if (property != null) {
        ReflectorFactory reflectorFactory = new DefaultReflectorFactory();
        MetaClass metaClass = MetaClass.forClass(this.parameterType, reflectorFactory);
        if (metaClass.hasGetter(property)) {
          propertyType = metaClass.getGetterType(property);
        } else {
          propertyType = Object.class;
        }
      } else {
        propertyType = Object.class;
      }
      ParameterMapping.Builder builder = new ParameterMapping.Builder(this.configuration, property, propertyType);
      ParameterMapping.buildParam(null, configuration, expression, builder);
      return builder.build();
    }

    private ParameterExpression parseParameterMapping(String content) {
      try {
        return new ParameterExpression(content);
      } catch (BuilderException ex) {
        throw ex;
      } catch (Exception ex) {
        throw new BuilderException("Parsing error was found in mapping @{" + content
          + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
      }
    }
  }
}
