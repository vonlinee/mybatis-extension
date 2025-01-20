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
package org.apache.ibatis.mapping;

import java.sql.ResultSet;
import java.util.Map;

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.ParameterExpression;
import org.apache.ibatis.internal.StringKey;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.jetbrains.annotations.Nullable;

/**
 * @author Clinton Begin
 */
public class ParameterMapping {

  private Configuration configuration;

  private String property;
  private ParameterMode mode;
  private Class<?> javaType = Object.class;
  private JdbcType jdbcType;
  private Integer numericScale;
  private TypeHandler<?> typeHandler;
  private String resultMapId;
  private String jdbcTypeName;
  private String expression;

  private ParameterMapping() {
  }

  public static class Builder {
    private final ParameterMapping parameterMapping = new ParameterMapping();

    public Builder(Configuration configuration, String property, TypeHandler<?> typeHandler) {
      parameterMapping.configuration = configuration;
      parameterMapping.property = property;
      parameterMapping.typeHandler = typeHandler;
      parameterMapping.mode = ParameterMode.IN;
    }

    public Builder(Configuration configuration, String property, Class<?> javaType) {
      parameterMapping.configuration = configuration;
      parameterMapping.property = property;
      parameterMapping.javaType = javaType;
      parameterMapping.mode = ParameterMode.IN;
    }

    public Builder mode(ParameterMode mode) {
      parameterMapping.mode = mode;
      return this;
    }

    public Builder javaType(Class<?> javaType) {
      parameterMapping.javaType = javaType;
      return this;
    }

    public Builder jdbcType(JdbcType jdbcType) {
      parameterMapping.jdbcType = jdbcType;
      return this;
    }

    public Builder numericScale(Integer numericScale) {
      parameterMapping.numericScale = numericScale;
      return this;
    }

    public Builder resultMapId(String resultMapId) {
      parameterMapping.resultMapId = resultMapId;
      return this;
    }

    public Builder typeHandler(TypeHandler<?> typeHandler) {
      parameterMapping.typeHandler = typeHandler;
      return this;
    }

    public Builder jdbcTypeName(String jdbcTypeName) {
      parameterMapping.jdbcTypeName = jdbcTypeName;
      return this;
    }

    public Builder expression(String expression) {
      parameterMapping.expression = expression;
      return this;
    }

    public ParameterMapping build() {
      resolveTypeHandler();
      validate();
      return parameterMapping;
    }

    private void validate() {
      if (ResultSet.class.equals(parameterMapping.javaType)) {
        if (parameterMapping.resultMapId == null) {
          throw new IllegalStateException("Missing result map in property '" + parameterMapping.property + "'.  "
            + "Parameters of type java.sql.ResultSet require a result map.");
        }
      } else if (parameterMapping.typeHandler == null) {
        throw new IllegalStateException("Type handler was null on parameter mapping for property '"
          + parameterMapping.property + "'. It was either not specified and/or could not be found for the javaType ("
          + parameterMapping.javaType.getName() + ") : jdbcType (" + parameterMapping.jdbcType + ") combination.");
      }
    }

    private void resolveTypeHandler() {
      if (parameterMapping.typeHandler == null && parameterMapping.javaType != null) {
        Configuration configuration = parameterMapping.configuration;
        TypeHandlerRegistry typeHandlerRegistry = configuration.getTypeHandlerRegistry();
        parameterMapping.typeHandler = typeHandlerRegistry.getTypeHandler(parameterMapping.javaType,
          parameterMapping.jdbcType);
      }
    }

  }

  public String getProperty() {
    return property;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the mode
   */
  public ParameterMode getMode() {
    return mode;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the java type
   */
  public Class<?> getJavaType() {
    return javaType;
  }

  /**
   * Used in the UnknownTypeHandler in case there is no handler for the property type.
   *
   * @return the jdbc type
   */
  public JdbcType getJdbcType() {
    return jdbcType;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the numeric scale
   */
  public Integer getNumericScale() {
    return numericScale;
  }

  /**
   * Used when setting parameters to the PreparedStatement.
   *
   * @return the type handler
   */
  public TypeHandler<?> getTypeHandler() {
    return typeHandler;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the result map id
   */
  public String getResultMapId() {
    return resultMapId;
  }

  /**
   * Used for handling output of callable statements.
   *
   * @return the jdbc type name
   */
  public String getJdbcTypeName() {
    return jdbcTypeName;
  }

  /**
   * Expression 'Not used'.
   *
   * @return the expression
   */
  public String getExpression() {
    return expression;
  }

  @Override
  public String toString() {
    final StringBuilder sb = new StringBuilder("ParameterMapping{");
    // sb.append("configuration=").append(configuration); // configuration doesn't have a useful .toString()
    sb.append("property='").append(property).append('\'');
    sb.append(", mode=").append(mode);
    sb.append(", javaType=").append(javaType);
    sb.append(", jdbcType=").append(jdbcType);
    sb.append(", numericScale=").append(numericScale);
    // sb.append(", typeHandler=").append(typeHandler); // typeHandler also doesn't have a useful .toString()
    sb.append(", resultMapId='").append(resultMapId).append('\'');
    sb.append(", jdbcTypeName='").append(jdbcTypeName).append('\'');
    sb.append(", expression='").append(expression).append('\'');
    sb.append('}');
    return sb.toString();
  }

  private static void buildParam(Class<?> javaType, Configuration configuration, ParameterExpression expression, ParameterMapping.Builder builder) {
    final String jdbcType = expression.getJdbcType();
    if (jdbcType != null) {
      builder.jdbcType(configuration.resolveJdbcType(jdbcType));
    }
    String typeHandlerAlias = null;

    if (expression.hasOption(StringKey.JAVA_TYPE)) {
      javaType = configuration.resolveClass(expression.getOption(StringKey.JAVA_TYPE));
      builder.javaType(javaType);
    }

    // jdbc type
    if (expression.getJdbcType() != null) {
      builder.jdbcType(configuration.resolveJdbcType(expression.getJdbcType()));
    }

    // mode
    if (expression.hasOption(StringKey.MODE)) {
      builder.mode(BaseBuilder.resolveParameterMode(expression.getOption(StringKey.MODE)));
    }

    // numericScale
    if (expression.hasOption(StringKey.NUMERIC_SCALE)) {
      builder.numericScale(Integer.valueOf(expression.getOption(StringKey.NUMERIC_SCALE)));
    }

    // result map
    if (expression.hasOption(StringKey.RESULT_MAP)) {
      builder.resultMapId(expression.getOption(StringKey.RESULT_MAP));
    }

    // type handler
    if (expression.hasOption(StringKey.TYPE_HANDLER)) {
      typeHandlerAlias = expression.getOption(StringKey.TYPE_HANDLER);
    }

    // jdbc type name
    if (expression.hasOption(StringKey.JDBC_TYPE_NAME)) {
      builder.jdbcTypeName(expression.getOption(StringKey.TYPE_HANDLER));
    }

    // expression
    if (expression.isExpression()) {
      throw new BuilderException("Expression based parameters are not supported yet");
    }
    // other unhandled property will be ignored.
    if (typeHandlerAlias != null) {
      builder.typeHandler(configuration.resolveTypeHandler(javaType, typeHandlerAlias));
    }
  }

  /**
   * #{property|(expression), var1=value1, var2=value2, ...}
   * or
   * ${property|(expression), var1=value1, var2=value2, ...}
   * or
   * {@literal @{property|(expression), var1=value1, var2=value2, ...}}
   *
   * @param content        content to parse.
   * @param config         configuration instance.
   * @param parameterType  parameter type hint to help parse process.
   * @param metaParameters additional parameters to help parse process.
   * @return ParameterMapping instance
   */
  public static ParameterMapping parse(String content, Configuration config, Class<?> parameterType, @Nullable MetaObject metaParameters) {
    ParameterExpression expression = new ParameterExpression(content);
    final String property = expression.getProperty();
    Class<?> propertyType;
    if (metaParameters != null && metaParameters.hasGetter(property)) { // issue #448 get type from additional params
      propertyType = metaParameters.getGetterType(property);
    } else if (config.hasTypeHandler(parameterType)) {
      propertyType = parameterType;
    } else if (JdbcType.CURSOR.name().equals(expression.getJdbcType())) {
      propertyType = java.sql.ResultSet.class;
    } else if (property == null || Map.class.isAssignableFrom(parameterType)) {
      propertyType = Object.class;
    } else {
      MetaClass metaClass = MetaClass.forClass(parameterType, config.getReflectorFactory());
      if (metaClass.hasGetter(property)) {
        propertyType = metaClass.getGetterType(property);
      } else {
        propertyType = Object.class;
      }
    }
    ParameterMapping.Builder builder = new ParameterMapping.Builder(config, property, propertyType);
    buildParam(propertyType, config, expression, builder);
    return builder.build();
  }
}
