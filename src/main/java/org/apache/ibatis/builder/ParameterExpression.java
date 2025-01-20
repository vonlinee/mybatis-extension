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

import org.apache.ibatis.internal.StringKey;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Inline parameter expression parser. Supported grammar (simplified):
 *
 * <pre>
 * inline-parameter = (propertyName | expression) oldJdbcType attributes
 * propertyName = /expression language's property navigation path/
 * expression = '(' /expression language's expression/ ')'
 * oldJdbcType = ':' /any valid jdbc type/
 * attributes = (',' attribute)*
 * attribute = name '=' value
 * </pre>
 *
 * @author Frank D. Martinez [mnesarco]
 */
public class ParameterExpression {

  private String jdbcType;
  private String property;
  private String expression;
  private Map<String, String> options;

  public ParameterExpression(String expression) {
    parse(expression);
  }

  public void addOption(String name, String value) {
    if (options == null) {
      this.options = new HashMap<>();
    }
    this.options.put(name, value);
  }

  /**
   * entry method
   *
   * @param expression expression
   */
  private void parse(String expression) {
    int p = skipWS(expression, 0);
    if (expression.charAt(p) == StringKey.OPEN_PARENTHESIS) {
      expression(expression, p + 1);
    } else {
      property(expression, p);
    }
  }

  private void expression(String expression, int left) {
    int match = 1;
    int right = left + 1;
    while (match > 0) {
      if (expression.charAt(right) == StringKey.CLOSE_PARENTHESIS) {
        match--;
      } else if (expression.charAt(right) == '(') {
        match++;
      }
      right++;
    }
    this.expression = expression.substring(left, right - 1);
    jdbcTypeOpt(expression, right);
  }

  private void property(String expression, int left) {
    if (left < expression.length()) {
      int right = skipUntil(expression, left, ",:");
      this.property = trimmedStr(expression, left, right);
      jdbcTypeOpt(expression, right);
    }
  }

  /**
   * Skips whitespace characters in the given expression starting from the specified index.
   *
   * @param expression The string in which to skip whitespace.
   * @param p          The starting index from which to begin skipping whitespace.
   * @return The index of the first non-whitespace character, or the length of the string
   * if no non-whitespace character is found.
   */
  private static int skipWS(String expression, int p) {
    for (int i = p; i < expression.length(); i++) {
      if (expression.charAt(i) > 0x20) {
        return i;
      }
    }
    return expression.length();
  }

  private static int skipUntil(String expression, int p, final String endChars) {
    for (int i = p; i < expression.length(); i++) {
      char c = expression.charAt(i);
      if (endChars.indexOf(c) > -1) {
        return i;
      }
    }
    return expression.length();
  }

  private void jdbcTypeOpt(String expression, int p) {
    p = skipWS(expression, p);
    if (p < expression.length()) {
      if (expression.charAt(p) == StringKey.COLON) {
        jdbcType(expression, p + 1);
      } else if (expression.charAt(p) == StringKey.ENGLISH_COMMA) {
        option(expression, p + 1);
      } else {
        throw new BuilderException("Parsing error in {" + expression + "} in position " + p);
      }
    }
  }

  private void jdbcType(String expression, int p) {
    int left = skipWS(expression, p);
    int right = skipUntil(expression, left, ",");
    if (right <= left) {
      throw new BuilderException("Parsing error in {" + expression + "} in position " + p);
    }
    this.jdbcType = trimmedStr(expression, left, right);
    option(expression, right + 1);
  }

  private void option(String expression, int p) {
    int left = skipWS(expression, p);
    if (left < expression.length()) {
      int right = skipUntil(expression, left, "=");
      String name = trimmedStr(expression, left, right);
      left = right + 1;
      right = skipUntil(expression, left, ",");
      String value = trimmedStr(expression, left, right);
      addOption(name, value);
      option(expression, right + 1);
    }
  }

  private static String trimmedStr(String str, int start, int end) {
    while (str.charAt(start) <= 0x20) {
      start++;
    }
    while (str.charAt(end - 1) <= 0x20) {
      end--;
    }
    return start >= end ? "" : str.substring(start, end);
  }

  public String getProperty() {
    return property;
  }

  public String getJdbcType() {
    return jdbcType;
  }

  public String getExpression() {
    return expression;
  }

  public String getMode() {
    return getOption(StringKey.MODE);
  }

  public String getOption(String name) {
    if (options == null) {
      return null;
    }
    return options.get(name);
  }

  public boolean hasOption(String name) {
    return options.containsKey(name);
  }

  public boolean isExpression() {
    return this.expression != null;
  }

  public static ParameterExpression parseParameterMapping(String content, Function<String, String> errorMessageSupplier) {
    try {
      return new ParameterExpression(content);
    } catch (BuilderException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new BuilderException("Parsing error was found in mapping #{" + content
        + "}.  Check syntax #{property|(expression), var1=value1, var2=value2, ...} ", ex);
    }
  }

  public static ParameterExpression parseContent(String content, String identifier) {
    try {
      return new ParameterExpression(content);
    } catch (BuilderException ex) {
      throw ex;
    } catch (Exception ex) {
      throw new BuilderException(String.format("Parsing error was found in mapping %s{" + content
        + "}.  Check syntax %s{property|(expression), var1=value1, var2=value2, ...} ", identifier, identifier), ex);
    }
  }
}
