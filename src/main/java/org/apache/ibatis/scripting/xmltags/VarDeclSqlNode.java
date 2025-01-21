/*
 *    Copyright 2009-2022 the original author or authors.
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
import org.apache.ibatis.scripting.ExpressionEvaluator;

/**
 * @author Frank D. Martinez [mnesarco]
 */
public class VarDeclSqlNode extends DynamicSqlNode {

  private final String name;
  private final String expression;

  public VarDeclSqlNode(ExpressionEvaluator evaluator, String name, String exp) {
    this.evaluator = evaluator;
    this.name = name;
    this.expression = exp;
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    final Object value = evaluator.getValue(expression, context.getBindings().asMap());
    context.bind(name, value);
    return true;
  }

}
