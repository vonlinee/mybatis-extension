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

import org.apache.ibatis.builder.BaseBuilder;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.session.Configuration;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Clinton Begin
 */
public class XMLScriptBuilder extends BaseBuilder {

  private boolean isDynamic;
  private final boolean nullableOnForEach;

  @NotNull
  private final ExpressionEvaluator evaluator;

  private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();

  public XMLScriptBuilder(@NotNull ExpressionEvaluator evaluator, boolean nullableOnForEach, Configuration configuration) {
    super(configuration);
    this.nullableOnForEach = nullableOnForEach;
    this.evaluator = evaluator;
    nodeHandlerMap.put("trim", new TrimHandler());
    nodeHandlerMap.put("where", new WhereHandler());
    nodeHandlerMap.put("set", new SetHandler());

    nodeHandlerMap.put("foreach", new ForEachHandler(evaluator, this.nullableOnForEach));
    nodeHandlerMap.put("if", new IfHandler(evaluator));
    nodeHandlerMap.put("choose", new ChooseHandler());
    nodeHandlerMap.put("when", new IfHandler(evaluator));
    nodeHandlerMap.put("otherwise", new OtherwiseHandler());
    nodeHandlerMap.put("bind", new BindHandler(evaluator));
  }

  public boolean isNullableOnForEach() {
    return nullableOnForEach;
  }

  public SqlSource parseScriptNode(XNode context, Class<?> parameterType) {
    // 解析动态标签
    MixedSqlNode rootSqlNode = parseDynamicTags(context);
    SqlSource sqlSource;
    if (isDynamic) {
      sqlSource = new DynamicSqlSource(configuration, rootSqlNode);
    } else {
      sqlSource = new RawSqlSource(configuration, rootSqlNode, parameterType);
    }
    return sqlSource;
  }

  /**
   * 动态标签解析实现
   *
   * @param node root xml node
   * @return MixedSqlNode
   */
  public MixedSqlNode parseDynamicTags(XNode node) {
    List<SqlNode> contents = new ArrayList<>();
    NodeList children = node.getNode().getChildNodes();

    for (int i = 0; i < children.getLength(); i++) {
      XNode child = node.newXNode(children.item(i));
      if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
        String data = child.getStringBody("");
        TextSqlNode textSqlNode = new TextSqlNode(data, evaluator);
        // 判断文本节点中是否包含了${}，如果包含则为动态文本节点，否则为静态文本节点
        // 静态文本节点在运行时不需要二次处理
        if (textSqlNode.isDynamic()) {
          contents.add(textSqlNode);
          isDynamic = true;
        } else {
          contents.add(new StaticTextSqlNode(data));
        }
      } else if (child.getNode().getNodeType() == Node.ELEMENT_NODE) { // issue #628
        String nodeName = child.getNode().getNodeName();
        NodeHandler handler = nodeHandlerMap.get(nodeName);
        if (handler == null) {
          throw new BuilderException("Unknown element <" + nodeName + "> in SQL statement.");
        }
        handler.handleNode(child, contents);
        isDynamic = true;
      }
    }
    return new MixedSqlNode(contents);
  }

  private interface NodeHandler {

    @NotNull
    String getId();

    void handleNode(XNode nodeToHandle, List<SqlNode> targetContents);
  }

  /**
   * 元素可以使用表达式创建一个变量并将其绑定到当前SQL节点的上下文, 例如:
   * <blockquote><pre>
   * <select id="selectBlogsLike" parameterType="BlogQuery" resultType="Blog">
   *     <bind name="pattern" value="'%' + title + '%'" />
   *      SELECT * FROM BLOG
   *      WHERE title LIKE #{pattern}
   * </select>
   * </pre></blockquote>
   * <br/>
   * bind还可以用来预防 SQL 注入
   */
  private static class BindHandler implements NodeHandler {

    ExpressionEvaluator evaluator;

    public BindHandler(ExpressionEvaluator evaluator) {
      // Prevent Synthetic Access
      this.evaluator = evaluator;
    }

    @Override
    public @NotNull String getId() {
      return "bind";
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      // 变量名称
      final String name = nodeToHandle.getStringAttribute("name");
      // 表达式
      final String expression = nodeToHandle.getStringAttribute("value");
      final VarDeclSqlNode node = new VarDeclSqlNode(evaluator, name, expression);
      targetContents.add(node);
    }
  }

  /**
   * trim使用最多的情况是截掉where条件中的前置OR和AND，update的set子句中的后置”,”，同时在内容不为空的时候加上where和set。
   *
   * <blockquote><pre>
   * select * from user
   * <trim prefix="WHERE" prefixoverride="AND |OR">
   *     <if test="name != null and name.length()>0"> AND name=#{name}</if>
   *     <if test="gender != null and gender.length()>0"> AND gender=#{gender}</if>
   * </trim>
   * </pre></blockquote>
   *
   * <blockquote><pre>
   * update user
   * <trim prefix="set" suffixoverride="," suffix=" where id = #{id} ">
   *     <if test="name != null and name.length()>0"> name=#{name} , </if>
   *     <if test="gender != null and gender.length()>0"> gender=#{gender} ,  </if>
   * </trim>
   * </pre></blockquote>
   */
  private class TrimHandler implements NodeHandler {
    public TrimHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public @NotNull String getId() {
      return "trim";
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      // 包含的子节点解析后SQL文本不为空时要添加的前缀内容
      String prefix = nodeToHandle.getStringAttribute("prefix");
      // 要覆盖的子节点解析后SQL文本前缀内容
      String prefixOverrides = nodeToHandle.getStringAttribute("prefixOverrides");
      // 包含的子节点解析后SQL文本不为空时要添加的后缀内容
      String suffix = nodeToHandle.getStringAttribute("suffix");
      // 要覆盖的子节点解析后SQL文本后缀内容
      String suffixOverrides = nodeToHandle.getStringAttribute("suffixOverrides");
      TrimSqlNode trim = new TrimSqlNode(mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
      targetContents.add(trim);
    }
  }

  /**
   * 和set一样，where也是trim的特殊情况，同样where标签也不是必须的，可以通过其他方式解决。
   */
  private class WhereHandler implements NodeHandler {
    public WhereHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public @NotNull String getId() {
      return "where";
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      WhereSqlNode where = new WhereSqlNode(mixedSqlNode);
      targetContents.add(where);
    }
  }

  /**
   * set标签主要用于解决update动态字段, 例如:
   * <blockquote><pre>
   * <update id="updateAuthorIfNecessary">
   *   update Author
   *     <set>
   *       <if test="username != null">username=#{username},</if>
   *       <if test="password != null">password=#{password},</if>
   *       <if test="email != null">email=#{email},</if>
   *       <if test="bio != null">bio=#{bio}</if>
   *     </set>
   *   where id=#{id}
   * </update>
   * </pre></blockquote>
   */
  private class SetHandler implements NodeHandler {
    public SetHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public @NotNull String getId() {
      return "set";
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      SetSqlNode set = new SetSqlNode(mixedSqlNode);
      targetContents.add(set);
    }
  }

  /**
   * foreach可以将任何可迭代对象（如列表、集合等）和任何的字典或者数组对象传递给foreach作为集合参数。
   * <li>1. 当使用可迭代对象或者数组时，index是当前迭代的次数，item的值是本次迭代获取的元素。</li>
   * <li>2. 当使用字典（或者Map.Entry对象的集合）时，index是键, item是值。</li>
   *
   * <blockquote><pre>
   * <select id="selectPostIn" resultType="domain.blog.Post">
   *   SELECT *
   *   FROM POST P
   *   <where>
   *     <foreach item="item" index="index" collection="list"
   *         open="ID in (" separator="," close=")" nullable="true">
   *           #{item}
   *     </foreach>
   *   </where>
   * </select>
   * </pre></blockquote>
   */
  private class ForEachHandler implements NodeHandler {

    @NotNull
    private final ExpressionEvaluator evaluator;
    boolean nullableOnForeach;

    public ForEachHandler(@NotNull ExpressionEvaluator evaluator, boolean nullableOnForeach) {
      this.evaluator = evaluator;
      this.nullableOnForeach = nullableOnForeach;
    }

    @Override
    public @NotNull String getId() {
      return "foreach";
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      String collection = nodeToHandle.getStringAttribute("collection");
      Boolean nullable = nodeToHandle.getBooleanAttribute("nullable");
      String item = nodeToHandle.getStringAttribute("item");
      String index = nodeToHandle.getStringAttribute("index");
      String open = nodeToHandle.getStringAttribute("open");
      String close = nodeToHandle.getStringAttribute("close");
      String separator = nodeToHandle.getStringAttribute("separator");

      if (nullable == null) {
        nullable = this.nullableOnForeach;
      }

      ForEachSqlNode forEachSqlNode = new ForEachSqlNode(evaluator, mixedSqlNode, collection, nullable, index, item, open, close, separator);
      targetContents.add(forEachSqlNode);
    }
  }

  private class IfHandler implements NodeHandler {

    ExpressionEvaluator evaluator;

    public IfHandler(ExpressionEvaluator evaluator) {
      // Prevent Synthetic Access
      this.evaluator = evaluator;
    }

    @Override
    public @NotNull String getId() {
      return "if";
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      String test = nodeToHandle.getStringAttribute("test");
      IfSqlNode ifSqlNode = new IfSqlNode(evaluator, mixedSqlNode, test);
      targetContents.add(ifSqlNode);
    }
  }

  /**
   * otherwise标签不做任何处理，用在choose标签的最后默认分支
   */
  private class OtherwiseHandler implements NodeHandler {
    public OtherwiseHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public @NotNull String getId() {
      return "otherwise";
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      targetContents.add(mixedSqlNode);
    }
  }

  /**
   * choose节点应该说和switch是等价的，其中的when就是各种条件判断
   */
  private class ChooseHandler implements NodeHandler {
    public ChooseHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public @NotNull String getId() {
      return "choose";
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      List<SqlNode> whenSqlNodes = new ArrayList<>();
      List<SqlNode> otherwiseSqlNodes = new ArrayList<>();
      // 拆分出when 和 otherwise 节点
      handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes, otherwiseSqlNodes);
      SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
      ChooseSqlNode chooseSqlNode = new ChooseSqlNode(whenSqlNodes, defaultSqlNode);
      targetContents.add(chooseSqlNode);
    }

    private void handleWhenOtherwiseNodes(XNode chooseSqlNode, List<SqlNode> ifSqlNodes, List<SqlNode> defaultSqlNodes) {
      List<XNode> children = chooseSqlNode.getChildren();
      for (XNode child : children) {
        String nodeName = child.getNode().getNodeName();
        NodeHandler handler = nodeHandlerMap.get(nodeName);
        if (handler instanceof IfHandler) {
          handler.handleNode(child, ifSqlNodes);
        } else if (handler instanceof OtherwiseHandler) {
          handler.handleNode(child, defaultSqlNodes);
        }
      }
    }

    private SqlNode getDefaultSqlNode(List<SqlNode> defaultSqlNodes) {
      SqlNode defaultSqlNode = null;
      if (defaultSqlNodes.size() == 1) {
        defaultSqlNode = defaultSqlNodes.get(0);
      } else if (defaultSqlNodes.size() > 1) {
        throw new BuilderException("Too many default (otherwise) elements in choose statement.");
      }
      return defaultSqlNode;
    }
  }

}
