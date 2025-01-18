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

import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.scripting.BindingContext;
import org.apache.ibatis.scripting.SqlBuildContext;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.SqlBuildContextDelegator;
import org.apache.ibatis.scripting.ognl.OgnlExpressionEvaluator;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * To make an object that is used as an input parameter {@literal <foreach>}, the List object defaults to using "list" as the key,
 * the array object has "array" as the key, and the Map object does not have a default key. Of course, when used as
 * an input parameter, @ Param ("keyName") can be used to set the key. After setting the keyName, the list and array
 * will become invalid. In addition to the situation of parameter input, there is also a case where a certain field
 * is used as a parameter object. For example, if the User has the property List ids. If the input parameter is a
 * User object, then this collection="ids". If the User has the property Ids ids; Ids is an object, and Ids has a
 * property called List ID; If the input parameter is a User object, then collection="ids. id"
 * <p>
 * If a single parameter is passed in and the parameter type is a List, the collection property value is list
 * If a single parameter is passed in and the parameter type is an array, the collection property value is array
 * If multiple parameters are passed in, we need to encapsulate them into a map. Of course, a single parameter can
 * also be encapsulated into a map. In fact, if you pass in parameters, MyBatis will also encapsulate them into a map,
 * and the key of the map is the parameter name. Therefore, at this time, the collection property value is the key of
 * the passed List or array object in its own encapsulated map
 * <p>
 * Note that you can pass a List instance or array as a parameter object to MyBatis, and when you do so, MyBatis
 * will automatically wrap it in a Map and use the name as the key. The List instance will use "list" as the key,
 * while the array instance's key will be "array".
 *
 * @author Clinton Begin
 */
public class ForEachSqlNode extends DynamicSqlNode {
  public static final String ITEM_PREFIX = "__frch_";

  private final ExpressionEvaluator evaluator;

  private final String collectionExpression;

  /**
   * whether the element to iterate can be nullable
   */
  private final boolean nullable;
  private final SqlNode contents;

  /**
   * The starting symbol of the FHIR code is usually used in conjunction with 'close='.
   * Commonly used in in() and values(). This parameter is optional
   */
  private final String open;

  /**
   * The 'close' symbol in the 'exec' code is usually used in conjunction with 'open=' (').
   * It is commonly used in in() and values(). This parameter is optional.
   */
  private final String close;

  /**
   * The delimiter between elements, for example, when in(), separator="," will automatically
   * separate the elements with "," to avoid manually entering commas that can cause SQL errors,
   * such as in (1,2,). This parameter is optional.
   */
  private final String separator;

  /**
   * The alias of the element in the collection during iteration, this parameter is required
   */
  private final String item;

  /**
   * In lists and arrays, index is the index of an element, and in maps,
   * index is the key of an element. This parameter is optional
   */
  private final String index;

  /**
   * @since 3.5.9
   */
  public ForEachSqlNode(SqlNode contents, String collectionExpression, Boolean nullable,
                        String index, String item, String open, String close, String separator) {
    this.evaluator = new OgnlExpressionEvaluator();
    this.collectionExpression = collectionExpression;
    this.nullable = nullable;
    this.contents = contents;
    this.open = open;
    this.close = close;
    this.separator = separator;
    this.index = index;
    this.item = item;
  }

  @Override
  public String getName() {
    return "foreach";
  }

  @Nullable
  public Iterable<?> getCollection(SqlBuildContext context) {
    BindingContext bindings = context.getBindings();
    return evaluator.evaluateIterable(collectionExpression, bindings, nullable);
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    final Iterable<?> iterable = getCollection(context);
    if (iterable == null || !iterable.iterator().hasNext()) {
      return true;
    }
    boolean first = true;
    applyOpen(context);
    int i = 0;
    for (Object o : iterable) {
      SqlBuildContext oldContext = context;
      if (first || separator == null) {
        context = new PrefixedSqlBuildContext(context, "");
      } else {
        context = new PrefixedSqlBuildContext(context, separator);
      }
      int uniqueNumber = context.getUniqueNumber();
      // Issue #709
      if (o instanceof Map.Entry) {
        @SuppressWarnings("unchecked")
        Map.Entry<Object, Object> mapEntry = (Map.Entry<Object, Object>) o;
        applyIndex(context, mapEntry.getKey(), uniqueNumber);
        applyItem(context, mapEntry.getValue(), uniqueNumber);
      } else {
        applyIndex(context, i, uniqueNumber);
        applyItem(context, o, uniqueNumber);
      }
      contents.apply(new FilteredSqlBuildContext(context, index, item, uniqueNumber));
      if (first) {
        first = !((PrefixedSqlBuildContext) context).isPrefixApplied();
      }
      context = oldContext;
      i++;
    }
    applyClose(context);
    context.getBindings().remove(item);
    context.getBindings().remove(index);
    return true;
  }

  private void applyIndex(SqlBuildContext context, Object o, int i) {
    if (index != null) {
      context.bind(index, o);
      context.bind(itemizeItem(index, i), o);
    }
  }

  private void applyItem(SqlBuildContext context, Object o, int i) {
    if (item != null) {
      context.bind(item, o);
      context.bind(itemizeItem(item, i), o);
    }
  }

  private void applyOpen(SqlBuildContext context) {
    if (open != null) {
      context.appendSql(open);
    }
  }

  private void applyClose(SqlBuildContext context) {
    if (close != null) {
      context.appendSql(close);
    }
  }

  private static String itemizeItem(String item, int i) {
    return ITEM_PREFIX + item + "_" + i;
  }

  private static class FilteredSqlBuildContext extends SqlBuildContextDelegator {
    private final SqlBuildContext delegate;
    private final int index;
    private final String itemIndex;
    private final String item;

    public FilteredSqlBuildContext(SqlBuildContext delegate, String itemIndex, String item,
                                   int i) {
      super(delegate);
      this.delegate = delegate;
      this.index = i;
      this.itemIndex = itemIndex;
      this.item = item;
    }

    @Override
    public void appendSql(String sql) {
      GenericTokenParser parser = new GenericTokenParser("#{", "}", content -> {
        String newContent = content.replaceFirst("^\\s*" + item + "(?![^.,:\\s])", itemizeItem(item, index));
        if (itemIndex != null && newContent.equals(content)) {
          newContent = content.replaceFirst("^\\s*" + itemIndex + "(?![^.,:\\s])", itemizeItem(itemIndex, index));
        }
        return "#{" + newContent + "}";
      });
      delegate.appendSql(parser.parse(sql));
    }
  }

  private static class PrefixedSqlBuildContext extends SqlBuildContextDelegator {
    private final SqlBuildContext delegate;
    private final String prefix;
    private boolean prefixApplied;

    public PrefixedSqlBuildContext(SqlBuildContext delegate, String prefix) {
      super(delegate);
      this.delegate = delegate;
      this.prefix = prefix;
      this.prefixApplied = false;
    }

    public boolean isPrefixApplied() {
      return prefixApplied;
    }

    @Override
    public void appendSql(String sql) {
      if (!prefixApplied && sql != null && !sql.trim().isEmpty()) {
        delegate.appendSql(prefix);
        prefixApplied = true;
      }
      delegate.appendSql(sql);
    }
  }
}
