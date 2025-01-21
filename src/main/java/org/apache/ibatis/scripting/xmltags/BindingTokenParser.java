package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.internal.StringKey;
import org.apache.ibatis.parsing.TokenHandler;
import org.apache.ibatis.scripting.BindingContext;
import org.apache.ibatis.scripting.SqlBuildContext;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.ScriptingException;
import org.apache.ibatis.type.SimpleTypeRegistry;

import java.util.regex.Pattern;

public class BindingTokenParser implements TokenHandler {

  private final SqlBuildContext context;
  private final Pattern injectionFilter;
  private final ExpressionEvaluator evaluator;

  public BindingTokenParser(ExpressionEvaluator evaluator, SqlBuildContext context, Pattern injectionFilter) {
    this.evaluator = evaluator;
    this.context = context;
    this.injectionFilter = injectionFilter;
  }

  @Override
  public String handleToken(String content) {
    BindingContext bindings = this.context.getBindings();
    Object parameter = bindings.get(SqlBuildContext.PARAMETER_OBJECT_KEY);
    if (parameter == null) {
      this.context.bind(StringKey.VALUE, null);
    } else if (SimpleTypeRegistry.isSimpleType(parameter.getClass())) {
      this.context.bind(StringKey.VALUE, parameter);
    }
    Object value = evaluator.getValue(content, bindings);
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
