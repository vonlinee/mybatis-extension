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

import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.MapBinding;
import org.apache.ibatis.scripting.ognl.OgnlExpressionEvaluator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * <pre>{@code
 * 	UPDATE author
 * 	<set>
 * 		<if test="username != null>
 * 			username = #{username},
 * 		</if>
 * 		<if test="password != null">
 * 		 	password = #{password}
 * 		</if>
 * 	</set>
 * 	WHERE id = #{id}
 * }</pre>
 *
 * @author <a href="1181963012mw@gmail.com">mawen12</a>
 *
 * @see <a href="https://mybatis.org/mybatis-3/dynamic-sql.html#trim-where-set">trim-where-set</a>
 */
class SetSqlNodeTest extends SqlNodeTest {

  private static final String FIRST_TEXT = " username = #{username},";
  private static final String SECOND_TEXT = " password = #{password}";

  private final ExpressionEvaluator evaluator = new OgnlExpressionEvaluator();
  private SqlNode sqlNode;

  @BeforeEach
  void setup() {
    SqlNode first = new IfSqlNode(evaluator, new StaticTextSqlNode(FIRST_TEXT), "username != null");
    SqlNode second = new IfSqlNode(evaluator, new StaticTextSqlNode(SECOND_TEXT), "password != null");
    SqlNode contents = new MixedSqlNode(Arrays.asList(first, second));

    this.sqlNode = new SetSqlNode(contents);
  }

  @Test
  @Override
  public void shouldApply() throws Exception {
    when(context.getBindings()).thenReturn(new MapBinding() {
      {
        put("username", "Jack");
        put("password", "***");
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("SET username = #{username}, password = #{password}");
  }

  @Test
  public void shouldAppendOnlyUsername() throws Exception {
    when(context.getBindings()).thenReturn(new MapBinding() {
      {
        put("username", "Jack");
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("SET username = #{username}");
  }

  @Test
  public void shouldAppendOnlyPassword() throws Exception {
    when(context.getBindings()).thenReturn(new MapBinding() {
      {
        put("password", "***");
      }
    });

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("SET password = #{password}");
  }

  @Test
  public void shouldAppendNone() throws Exception {
    when(context.getBindings()).thenReturn(new MapBinding());

    boolean result = sqlNode.apply(context);

    assertTrue(result);
    verify(context).appendSql("");
  }
}
