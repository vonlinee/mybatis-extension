/*
 *    Copyright 2009-2024 the original author or authors.
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
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.ognl.OgnlExpressionEvaluator;
import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 */
class TextSqlNodeTest extends SqlNodeTest {

  private static final String TEXT = "select 1 from dual";
  private static final String DYNAMIC_TEXT = "select * from user where id = ${id}";

  ExpressionEvaluator evaluator = new OgnlExpressionEvaluator();

  @Test
  @Override
  public void shouldApply() throws Exception {
    // given
    TextSqlNode sqlNode = new TextSqlNode(evaluator, TEXT);

    // when
    boolean result = sqlNode.apply(context);

    // then
    assertTrue(result);
    assertFalse(DynamicCheckerTokenParser.isDynamic(TEXT));
    verify(context).appendSql(TEXT);
  }

  @Test
  public void shouldApplyDynamic() {
    // given
    TextSqlNode sqlNode = new TextSqlNode(evaluator, DYNAMIC_TEXT);
    when(context.getBindings()).thenReturn(new HashMap<>() {
      {
        put("id", 1);
      }
    });

    // when
    boolean result = sqlNode.apply(context);

    // then
    assertTrue(result);
    assertTrue(DynamicCheckerTokenParser.isDynamic(sqlNode.getText()));
    verify(context).appendSql("select * from user where id = 1");
  }
}
