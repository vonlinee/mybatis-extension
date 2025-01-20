package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.BindingContext;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParameterMappingTokenHandler implements TokenHandler {

  private final List<ParameterMapping> parameterMappings = new ArrayList<>();
  private final Class<?> parameterType;
  private final MetaObject metaParameters;
  private final Configuration configuration;

  public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType,
                                      BindingContext additionalParameters) {
    this.configuration = configuration;
    this.parameterType = parameterType;
    this.metaParameters = configuration.newMetaObject(additionalParameters);
  }

  public List<ParameterMapping> getParameterMappings() {
    return parameterMappings;
  }

  /**
   * @param content #{content}
   * @return ? for prepared sql
   */
  @Override
  public String handleToken(String content) {
    parameterMappings.add(buildParameterMapping(content));
    return "?";
  }

  private ParameterMapping buildParameterMapping(String content) {
    ParameterExpression expression = parseParameterMapping(content);
    final String property = expression.getProperty();
    Class<?> propertyType;
    if (metaParameters.hasGetter(property)) { // issue #448 get type from additional params
      propertyType = metaParameters.getGetterType(property);
    } else if (configuration.hasTypeHandler(parameterType)) {
      propertyType = parameterType;
    } else if (JdbcType.CURSOR.name().equals(expression.getJdbcType())) {
      propertyType = java.sql.ResultSet.class;
    } else if (property == null || Map.class.isAssignableFrom(parameterType)) {
      propertyType = Object.class;
    } else {
      MetaClass metaClass = MetaClass.forClass(parameterType, configuration.getReflectorFactory());
      if (metaClass.hasGetter(property)) {
        propertyType = metaClass.getGetterType(property);
      } else {
        propertyType = Object.class;
      }
    }
    ParameterMapping.Builder builder = new ParameterMapping.Builder(configuration, property, propertyType);
    ParameterMapping.buildParam(propertyType, configuration, expression, builder);
    return builder.build();
  }

  private ParameterExpression parseParameterMapping(String content) {
    try {
      return new ParameterExpression(content);
    } catch (BuilderException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new BuilderException("Parsing error was found in mapping #{" + content
        + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
    }
  }
}
