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
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

/**
 * TextSqlNode主要是用来将${}转换为实际的参数值，并返回拼接后的SQL语句
 *
 * @author Clinton Begin
 * @apiNote 为了防止SQL注入，可以通过标签来创建OGNL上下文变量。
 */
public class TextSqlNode implements SqlNode {
  private final String text;
  private final Pattern injectionFilter;
  private final ExpressionEvaluator evaluator;

  public TextSqlNode(String text, ExpressionEvaluator evaluator) {
    this(text, evaluator, null);
  }

  public TextSqlNode(String text, ExpressionEvaluator evaluator, Pattern injectionFilter) {
    this.text = text;
    this.evaluator = evaluator;
    this.injectionFilter = injectionFilter;
  }

  public boolean isDynamic() {
    return GenericTokenParser.isDynamic(text, "${", "}");
  }

  @Override
  public boolean apply(DynamicContext context) {
    BindingTokenParser handler = new BindingTokenParser(context, injectionFilter, evaluator);
    context.appendSql(GenericTokenParser.parse(text, "${", "}", handler));
    return true;
  }

  private static class BindingTokenParser implements TokenHandler {

    private final DynamicContext context;
    private final Pattern injectionFilter;
    private final ExpressionEvaluator evaluator;

    public BindingTokenParser(DynamicContext context, Pattern injectionFilter, ExpressionEvaluator evaluator) {
      this.context = context;
      this.injectionFilter = injectionFilter;
      this.evaluator = evaluator;
    }

    /**
     * 将${}中的值替换为查询参数中实际的值并返回，在StaticTextSqlNode中，#{}返回的是?
     *
     * @param content 文本
     * @return
     */
    @Override
    public String handleToken(String content) {
      Object parameter = context.getBindings().get(DynamicContext.PARAMETER_OBJECT_KEY);
      if (parameter == null) {
        context.getBindings().put(DynamicContext.VALUE_KEY, null);
      } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
        context.getBindings().put(DynamicContext.VALUE_KEY, parameter);
      }
      Object value = evaluator.getValue(content, context.getBindings());
      String srtValue = value == null ? "" : String.valueOf(value); // issue #274 return "" instead of "null"
      checkInjection(srtValue);
      return srtValue;
    }

    private void checkInjection(String value) {
      if (injectionFilter != null && !injectionFilter.matcher(value).matches()) {
        throw new ScriptingException("Invalid input. Please conform to regex" + injectionFilter.pattern());
      }
    }
  }
}
