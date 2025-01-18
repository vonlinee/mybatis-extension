package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.internal.StringKey;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.scripting.SqlBuilderContext;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

public class BindingTokenParser implements TokenHandler {

  private final SqlBuilderContext context;
  private final Pattern injectionFilter;
  private final ExpressionEvaluator evaluator;

  public BindingTokenParser(ExpressionEvaluator evaluator, SqlBuilderContext context, Pattern injectionFilter) {
    this.evaluator = evaluator;
    this.context = context;
    this.injectionFilter = injectionFilter;
  }

  @Override
  public String handleToken(String content) {
    Object parameter = context.getBindings().get(SqlBuilderContext.PARAMETER_OBJECT_KEY);
    if (parameter == null) {
      context.bind(StringKey.VALUE, null);
    } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
      context.bind(StringKey.VALUE, parameter);
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
