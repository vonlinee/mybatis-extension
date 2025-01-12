package org.apache.ibatis.builder;

import org.apache.ibatis.internal.Constants;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ParameterMappingTokenHandler implements TokenHandler {

  private static final String PARAMETER_PROPERTIES = "javaType,jdbcType,mode,numericScale,resultMap,typeHandler,jdbcTypeName";

  private final List<ParameterMapping> parameterMappings = new ArrayList<>();
  private final Class<?> parameterType;
  private final MetaObject metaParameters;
  private final Configuration configuration;

  public ParameterMappingTokenHandler(Configuration configuration, Class<?> parameterType,
                                      Map<String, Object> additionalParameters) {
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
    Map<String, String> propertiesMap = parseParameterMapping(content);
    String property = propertiesMap.get(Constants.PROPERTY);
    Class<?> propertyType;
    if (metaParameters.hasGetter(property)) { // issue #448 get type from additional params
      propertyType = metaParameters.getGetterType(property);
    } else if (configuration.hasTypeHandler(parameterType)) {
      propertyType = parameterType;
    } else if (JdbcType.CURSOR.name().equals(propertiesMap.get(Constants.JDBC_TYPE))) {
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
    Class<?> javaType = propertyType;
    String typeHandlerAlias = null;
    for (Map.Entry<String, String> entry : propertiesMap.entrySet()) {
      String name = entry.getKey();
      String value = entry.getValue();
      if (Constants.JAVA_TYPE.equals(name)) {
        javaType = configuration.resolveClass(value);
        builder.javaType(javaType);
      } else if (Constants.JDBC_TYPE.equals(name)) {
        builder.jdbcType(configuration.resolveJdbcType(value));
      } else if (Constants.MODE.equals(name)) {
        builder.mode(BaseBuilder.resolveParameterMode(value));
      } else if (Constants.NUMERIC_SCALE.equals(name)) {
        builder.numericScale(Integer.valueOf(value));
      } else if (Constants.RESULT_MAP.equals(name)) {
        builder.resultMapId(value);
      } else if (Constants.TYPE_HANDLER.equals(name)) {
        typeHandlerAlias = value;
      } else if (Constants.JDBC_TYPE_NAME.equals(name)) {
        builder.jdbcTypeName(value);
      } else if (Constants.PROPERTY.equals(name)) {
        // Do Nothing
      } else if (Constants.EXPRESSION.equals(name)) {
        throw new BuilderException("Expression based parameters are not supported yet");
      } else {
        throw new BuilderException("An invalid property '" + name + "' was found in mapping #{" + content
          + "}.  Valid properties are " + PARAMETER_PROPERTIES);
      }
    }
    if (typeHandlerAlias != null) {
      builder.typeHandler(configuration.resolveTypeHandler(javaType, typeHandlerAlias));
    }
    return builder.build();
  }

  private Map<String, String> parseParameterMapping(String content) {
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
