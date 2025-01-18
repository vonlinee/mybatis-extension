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

import org.apache.ibatis.scripting.BindingContext;
import org.apache.ibatis.scripting.SqlBuilderContext;
import org.apache.ibatis.scripting.SqlBuilderContextDelegator;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Clinton Begin
 */
public class TrimSqlNode implements SqlNode {

  @NotNull
  private final SqlNode contents;
  private final String prefix;
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

  @Override
  public boolean apply(SqlBuilderContext context) {
    FilteredSqlBuilderContext filteredDynamicContext = new FilteredSqlBuilderContext(context);
    boolean result = contents.apply(filteredDynamicContext);
    filteredDynamicContext.applyAll();
    return result;
  }

  private static List<String> parseOverrides(String overrides) {
    if (overrides != null) {
      final StringTokenizer parser = new StringTokenizer(overrides, "|", false);
      final List<String> list = new ArrayList<>(parser.countTokens());
      while (parser.hasMoreTokens()) {
        list.add(parser.nextToken().toUpperCase(Locale.ENGLISH));
      }
      return list;
    }
    return Collections.emptyList();
  }

  private class FilteredSqlBuilderContext extends SqlBuilderContextDelegator {
    private final SqlBuilderContext delegate;
    private boolean prefixApplied;
    private boolean suffixApplied;
    private StringBuilder sqlBuffer;

    public FilteredSqlBuilderContext(SqlBuilderContext delegate) {
      super(delegate);
      this.delegate = delegate;
      this.prefixApplied = false;
      this.suffixApplied = false;
      this.sqlBuffer = new StringBuilder();
    }

    public void applyAll() {
      sqlBuffer = new StringBuilder(sqlBuffer.toString().trim());
      String trimmedUppercaseSql = sqlBuffer.toString().toUpperCase(Locale.ENGLISH);
      if (!trimmedUppercaseSql.isEmpty()) {
        applyPrefix(sqlBuffer, trimmedUppercaseSql);
        applySuffix(sqlBuffer, trimmedUppercaseSql);
      }
      delegate.appendSql(sqlBuffer.toString());
    }

    @Override
    public BindingContext getBindings() {
      return delegate.getBindings();
    }

    @Override
    public void bind(String name, Object value) {
      delegate.bind(name, value);
    }

    @Override
    public int getUniqueNumber() {
      return delegate.getUniqueNumber();
    }

    @Override
    public void appendSql(String sql) {
      sqlBuffer.append(sql);
    }

    @Override
    public String getSql() {
      return delegate.getSql();
    }

    private void applyPrefix(StringBuilder sql, String trimmedUppercaseSql) {
      if (prefixApplied) {
        return;
      }
      prefixApplied = true;
      if (prefixesToOverride != null) {
        prefixesToOverride.stream().filter(trimmedUppercaseSql::startsWith).findFirst()
          .ifPresent(toRemove -> sql.delete(0, toRemove.trim().length()));
      }
      if (prefix != null) {
        sql.insert(0, " ").insert(0, prefix);
      }
    }

    private void applySuffix(StringBuilder sql, String trimmedUppercaseSql) {
      if (suffixApplied) {
        return;
      }
      suffixApplied = true;
      if (suffixesToOverride != null) {
        suffixesToOverride.stream()
          .filter(toRemove -> trimmedUppercaseSql.endsWith(toRemove) || trimmedUppercaseSql.endsWith(toRemove.trim()))
          .findFirst().ifPresent(toRemove -> {
            int start = sql.length() - toRemove.trim().length();
            int end = sql.length();
            sql.delete(start, end);
          });
      }
      if (suffix != null) {
        sql.append(" ").append(suffix);
      }
    }

  }

}
