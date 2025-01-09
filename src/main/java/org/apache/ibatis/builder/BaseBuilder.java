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

import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.mapping.ResultSetType;
import org.apache.ibatis.session.Configuration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Clinton Begin
 */
public abstract class BaseBuilder {
  protected final Configuration configuration;

  public BaseBuilder(Configuration configuration) {
    this.configuration = configuration;
  }

  public static Pattern parseExpression(String regex, String defaultValue) {
    return Pattern.compile(regex == null ? defaultValue : regex);
  }

  public static Boolean booleanValueOf(String value, Boolean defaultValue) {
    return value == null ? defaultValue : Boolean.valueOf(value);
  }

  public static Integer integerValueOf(String value, Integer defaultValue) {
    return value == null ? defaultValue : Integer.valueOf(value);
  }

  public static Set<String> stringSetValueOf(String value, String defaultValue) {
    value = value == null ? defaultValue : value;
    return new HashSet<>(Arrays.asList(value.split(",")));
  }

  public static ResultSetType resolveResultSetType(String alias) {
    try {
      return alias == null ? null : ResultSetType.valueOf(alias);
    } catch (IllegalArgumentException e) {
      throw new BuilderException("Error resolving ResultSetType. Cause: " + e, e);
    }
  }

  public static ParameterMode resolveParameterMode(String alias) {
    try {
      return alias == null ? null : ParameterMode.findByAlias(alias);
    } catch (IllegalArgumentException e) {
      throw new BuilderException("Error resolving ParameterMode. Cause: " + e, e);
    }
  }
}
