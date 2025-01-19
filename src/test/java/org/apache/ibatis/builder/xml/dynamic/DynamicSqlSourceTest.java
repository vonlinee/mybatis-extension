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
package org.apache.ibatis.builder.xml.dynamic;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.ognl.OgnlExpressionEvaluator;
import org.apache.ibatis.scripting.xmltags.ChooseSqlNode;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.scripting.xmltags.ForEachSqlNode;
import org.apache.ibatis.scripting.xmltags.IfSqlNode;
import org.apache.ibatis.scripting.xmltags.MixedSqlNode;
import org.apache.ibatis.scripting.xmltags.SetSqlNode;
import org.apache.ibatis.scripting.xmltags.SqlNode;
import org.apache.ibatis.scripting.xmltags.TextSqlNode;
import org.apache.ibatis.scripting.xmltags.WhereSqlNode;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.Reader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicSqlSourceTest extends BaseDataTest {

  ExpressionEvaluator evaluator = new OgnlExpressionEvaluator();

  Configuration config = new Configuration();

  @Test
  void shouldDemonstrateSimpleExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    final MixedSqlNode sqlNode = mixedContents(new TextSqlNode(evaluator, expected));
    DynamicSqlSource source = createDynamicSqlSource(sqlNode);
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldDemonstrateMultipartExpectedTextWithNoLoopsOrConditionals() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new TextSqlNode(evaluator, "WHERE ID = ?"));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyIncludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "WHERE ID = ?")), "true"));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyExcludeWhere() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "WHERE ID = ?")), "false"));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyDefault() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'DEFAULT'";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new ChooseSqlNode(new ArrayList<>(Arrays.asList(
        new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = ?")), "false"),
        new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = 'NONE'")), "false")
      )), mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyChooseFirst() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new ChooseSqlNode(new ArrayList<>(Arrays.asList
        (
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = ?")), "true"),
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = 'NONE'")), "false")
        )
      )
        , mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldConditionallyChooseSecond() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE CATEGORY = 'NONE'";

    ArrayList<SqlNode> list = new ArrayList<>();
    list.add(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = ?")), "false"));
    list.add(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = 'NONE'")), "true"));
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new ChooseSqlNode(list, mixedContents(new TextSqlNode(evaluator, "WHERE CATEGORY = 'DEFAULT'"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREInsteadOfANDForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new WhereSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   and ID = ?  ")), "true"),
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   or NAME = ?  ")), "false"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREANDWithLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new WhereSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   and\n ID = ?  ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREANDWithCRLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \r\n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new WhereSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   and\r\n ID = ?  ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREANDWithTABForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \t ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new WhereSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   and\t ID = ?  ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREORWithLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"), new WhereSqlNode(mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   or\n ID = ?  ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREORWithCRLFForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \r\n ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new WhereSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   or\r\n ID = ?  ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREORWithTABForFirstCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE \t ID = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"), new WhereSqlNode(
      mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   or\t ID = ?  ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREInsteadOfORForSecondCondition() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new WhereSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   and ID = ?  ")), "false"),
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   or NAME = ?  ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimWHEREInsteadOfANDForBothConditions() throws Exception {
    final String expected = "SELECT * FROM BLOG WHERE  ID = ?   OR NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new WhereSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   and ID = ?   ")), "true"),
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "OR NAME = ?  ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimNoWhereClause() throws Exception {
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new WhereSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   and ID = ?   ")), "false"),
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "OR NAME = ?  ")), "false"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimSETInsteadOfCOMMAForBothConditions() throws Exception {
    final String expected = "UPDATE BLOG SET ID = ?,  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "UPDATE BLOG"),
      new SetSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, " ID = ?, ")), "true"),
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, " NAME = ?, ")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimCommaAfterSET() throws Exception {
    final String expected = "UPDATE BLOG SET  NAME = ?";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "UPDATE BLOG"),
      new SetSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "ID = ?")), "false"),
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, ", NAME = ?")), "true"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldTrimNoSetClause() throws Exception {
    final String expected = "UPDATE BLOG";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "UPDATE BLOG"),
      new SetSqlNode(
        mixedContents(new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, "   , ID = ?   ")), "false"),
          new IfSqlNode(evaluator, mixedContents(new TextSqlNode(evaluator, ", NAME = ?  ")), "false"))));
    BoundSql boundSql = source.getBoundSql(config, null);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldIterateOnceForEachItemInCollection() throws Exception {
    final HashMap<String, String[]> parameterObject = new HashMap<>();
    parameterObject.put("array", new String[]{"one", "two", "three"});

    final String expected = "SELECT * FROM BLOG WHERE ID in (  one = ? AND two = ? AND three = ? )";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG WHERE ID in"),
      new ForEachSqlNode(mixedContents(new TextSqlNode(evaluator, "${item} = #{item}")), "array", false, "index",
        "item", "(", ")", "AND"));
    BoundSql boundSql = source.getBoundSql(config, parameterObject);
    assertEquals(expected, boundSql.getSql());
    assertEquals(3, boundSql.getParameterMappings().size());
    assertEquals("__frch_item_0", boundSql.getParameterMappings().get(0).getProperty());
    assertEquals("__frch_item_1", boundSql.getParameterMappings().get(1).getProperty());
    assertEquals("__frch_item_2", boundSql.getParameterMappings().get(2).getProperty());
  }

  @Test
  void shouldHandleOgnlExpression() throws Exception {
    final HashMap<String, String> parameterObject = new HashMap<>();
    parameterObject.put("name", "Steve");

    final String expected = "Expression test: 3 / yes.";
    DynamicSqlSource source = createDynamicSqlSource(
      new TextSqlNode(evaluator, "Expression test: ${name.indexOf('v')} / ${name in {'Bob', 'Steve'\\} ? 'yes' : 'no'}."));
    BoundSql boundSql = source.getBoundSql(config, parameterObject);
    assertEquals(expected, boundSql.getSql());
  }

  @Test
  void shouldSkipForEachWhenCollectionIsEmpty() throws Exception {
    final HashMap<String, Integer[]> parameterObject = new HashMap<>();
    parameterObject.put("array", new Integer[]{});
    final String expected = "SELECT * FROM BLOG";
    DynamicSqlSource source = createDynamicSqlSource(new TextSqlNode(evaluator, "SELECT * FROM BLOG"),
      new ForEachSqlNode(mixedContents(new TextSqlNode(evaluator, "#{item}")), "array", false, null, "item",
        "WHERE id in (", ")", ","));
    BoundSql boundSql = source.getBoundSql(config, parameterObject);
    assertEquals(expected, boundSql.getSql());
    assertEquals(0, boundSql.getParameterMappings().size());
  }

  @Test
  void shouldPerformStrictMatchOnForEachVariableSubstitution() throws Exception {
    final Map<String, Object> param = new HashMap<>();
    final Map<String, String> uuu = new HashMap<>();
    uuu.put("u", "xyz");
    List<Bean> uuuu = new ArrayList<>();
    uuuu.add(new Bean("bean id"));
    param.put("uuu", uuu);
    param.put("uuuu", uuuu);
    DynamicSqlSource source = createDynamicSqlSource(
      new TextSqlNode(evaluator, "INSERT INTO BLOG (ID, NAME, NOTE, COMMENT) VALUES"),
      new ForEachSqlNode(
        mixedContents(
          new TextSqlNode(evaluator, "#{uuu.u}, #{u.id}, #{ u,typeHandler=org.apache.ibatis.type.StringTypeHandler},"
            + " #{u:VARCHAR,typeHandler=org.apache.ibatis.type.StringTypeHandler}")),
        "uuuu", false, "uu", "u", "(", ")", ","));
    BoundSql boundSql = source.getBoundSql(config, param);
    assertEquals(4, boundSql.getParameterMappings().size());
    assertEquals("uuu.u", boundSql.getParameterMappings().get(0).getProperty());
    assertEquals("__frch_u_0.id", boundSql.getParameterMappings().get(1).getProperty());
    assertEquals("__frch_u_0", boundSql.getParameterMappings().get(2).getProperty());
    assertEquals("__frch_u_0", boundSql.getParameterMappings().get(3).getProperty());
  }

  private DynamicSqlSource createDynamicSqlSource(SqlNode... contents) throws IOException, SQLException {
    createBlogDataSource();
    final String resource = "org/apache/ibatis/builder/MapperConfig.xml";
    final Reader reader = Resources.getResourceAsReader(resource);
    SqlSessionFactory sqlMapper = new SqlSessionFactoryBuilder().build(reader);
    Configuration configuration = sqlMapper.getConfiguration();
    MixedSqlNode sqlNode = mixedContents(contents);
    return new DynamicSqlSource(sqlNode);
  }

  private MixedSqlNode mixedContents(SqlNode... contents) {
    return new MixedSqlNode(Arrays.asList(contents));
  }

  @Test
  void shouldMapNullStringsToEmptyStrings() {
    final String expected = "id=${id}";
    final MixedSqlNode sqlNode = mixedContents(new TextSqlNode(evaluator, expected));
    final DynamicSqlSource source = new DynamicSqlSource(sqlNode);
    String sql = source.getBoundSql(config, new Bean(null)).getSql();
    Assertions.assertEquals("id=", sql);
  }

  public static class Bean {
    public String id;

    Bean(String property) {
      this.id = property;
    }

    public String getId() {
      return id;
    }

    public void setId(String property) {
      this.id = property;
    }
  }

}
