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

import org.apache.ibatis.parsing.DynamicCheckerTokenParser;
import org.apache.ibatis.parsing.GenericTokenParser;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

/**
 * xml element that contains text node as children only, typically just some literal strings.
 * <p>
 * for example:
 * <blockquote><pre>
 * <select id="xxx">
 *   select * from t_user
 *   <if test="some expression">where user_id = 1</if>
 * </select>
 * </pre></blockquote>
 * the string (select * from t_user) will be parsed as a text sql node, while
 * the string (where user_id = 1) will be parsed as the body of a IfSqlNode.
 *
 * @author Clinton Begin
 * @see IfSqlNode
 */
public class TextSqlNode implements SqlNode {
  private final String text;
  private final Pattern injectionFilter;
  private ExpressionEvaluator evaluator;

  public TextSqlNode(ExpressionEvaluator evaluator, String text) {
    this(text, null);
    this.evaluator = evaluator;
  }

  public TextSqlNode(String text, Pattern injectionFilter) {
    this.text = text;
    this.injectionFilter = injectionFilter;
  }

  public boolean isDynamic() {
    return DynamicCheckerTokenParser.isDynamic(text);
  }

  @Override
  public boolean apply(DynamicContext context) {
    GenericTokenParser parser = createParser(new BindingTokenParser(this.evaluator, context, injectionFilter));
    context.appendSql(parser.parse(text));
    return true;
  }

  private GenericTokenParser createParser(TokenHandler handler) {
    return new GenericTokenParser("${", "}", handler);
  }

  private static class BindingTokenParser implements TokenHandler {

    private final DynamicContext context;
    private final Pattern injectionFilter;
    private final ExpressionEvaluator evaluator;

    public BindingTokenParser(ExpressionEvaluator evaluator, DynamicContext context, Pattern injectionFilter) {
      this.evaluator = evaluator;
      this.context = context;
      this.injectionFilter = injectionFilter;
    }

    @Override
    public String handleToken(String content) {
      Object parameter = context.getBindings().get(DynamicContext.PARAMETER_OBJECT_KEY);
      if (parameter == null) {
        context.getBindings().put("value", null);
      } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
        context.getBindings().put("value", parameter);
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

  public String getText() {
    return text;
  }

}
