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

import org.apache.ibatis.scripting.SqlBuildContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * @author Clinton Begin
 */
public class TrimSqlNode implements SqlNode {

  @NotNull
  private final SqlNode contents;

  /**
   * prefix to trim
   */
  private final String prefix;

  /**
   * suffix to trim
   */
  private final String suffix;

  private final List<String> prefixesToOverride;

  private final List<String> suffixesToOverride;

  public TrimSqlNode(SqlNode contents, String prefix, String prefixesToOverride,
                     String suffix, String suffixesToOverride) {
    this(contents, prefix, parseOverrides(prefixesToOverride), suffix,
      parseOverrides(suffixesToOverride));
  }

  protected TrimSqlNode(@NotNull SqlNode contents, String prefix, List<String> prefixesToOverride,
                        String suffix, List<String> suffixesToOverride) {
    this.contents = contents;
    this.prefix = prefix;
    this.prefixesToOverride = prefixesToOverride;
    this.suffix = suffix;
    this.suffixesToOverride = suffixesToOverride;
  }

  @Override
  public String getName() {
    return "trim";
  }

  @Override
  public boolean isDynamic() {
    return contents.isDynamic();
  }

  @NotNull
  public SqlNode getContents() {
    return contents;
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    BufferedSqlBuildContextDelegator childContext = new BufferedSqlBuildContextDelegator(context);
    boolean result = contents.apply(childContext);
    context.appendSql(trimOverrides(childContext.getSql(), this.prefix, this.suffix, this.prefixesToOverride, this.suffixesToOverride));
    return result;
  }

  public String trimOverrides(String sql, String prefix, String suffix, List<String> prefixesToOverride, List<String> suffixesToOverride) {
    StringBuilder sqlBuffer = new StringBuilder(sql.trim());
    String trimmedUppercaseSql = sqlBuffer.toString().toUpperCase(Locale.ENGLISH);
    if (!trimmedUppercaseSql.isEmpty()) {
      applyPrefix(sqlBuffer, trimmedUppercaseSql, prefix, prefixesToOverride);
      applySuffix(sqlBuffer, trimmedUppercaseSql, suffix, suffixesToOverride);
    }
    return sqlBuffer.toString();
  }

  void applyPrefix(StringBuilder sql, String trimmedUppercaseSql, String prefixToInsert, List<String> prefixesToOverride) {
    if (prefixesToOverride != null && !prefixesToOverride.isEmpty()) {
      for (String prefixToOverride : prefixesToOverride) {
        if (trimmedUppercaseSql.startsWith(prefixToOverride)) {
          sql.delete(0, prefixToOverride.trim().length());
          break;
        }
      }
    }
    if (prefixToInsert != null) {
      sql.insert(0, " ").insert(0, prefixToInsert);
    }
  }

  void applySuffix(StringBuilder sql, String trimmedUppercaseSql, String suffixToAppend, List<String> suffixesToOverride) {
    if (suffixesToOverride != null && !suffixesToOverride.isEmpty()) {
      for (String suffix : suffixesToOverride) {
        if (trimmedUppercaseSql.endsWith(suffix) || trimmedUppercaseSql.endsWith(suffix.trim())) {
          int start = sql.length() - suffix.trim().length();
          int end = sql.length();
          sql.delete(start, end);
          break;
        }
      }
    }
    if (suffixToAppend != null) {
      sql.append(" ").append(suffixToAppend);
    }
  }

  private static List<String> parseOverrides(String overrides) {
    if (overrides != null) {
      String[] tokens = overrides.split("\\|");
      final List<String> list = new ArrayList<>(tokens.length);
      for (String token : tokens) {
        list.add(token.toUpperCase(Locale.ENGLISH));
      }
      return list;
    }
    return Collections.emptyList();
  }
}
