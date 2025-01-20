package org.apache.ibatis.builder;

import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.scripting.BindingContext;
import org.apache.ibatis.session.Configuration;

import java.util.ArrayList;
import java.util.List;

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
    try {
      return ParameterMapping.parse(content, configuration, parameterType, metaParameters);
    } catch (BuilderException exception) {
      throw exception;
    } catch (Exception ex) {
      throw new BuilderException("Parsing error was found in mapping #{" + content
        + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
    }
  }
}
