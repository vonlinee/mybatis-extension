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
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.internal.StringKey;
import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.DynamicCheckerTokenParser;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.scripting.ExpressionEvaluator;
import org.apache.ibatis.scripting.MapBinding;
import org.apache.ibatis.scripting.defaults.RawSqlSource;
import org.apache.ibatis.scripting.ognl.OgnlExpressionEvaluator;
import org.apache.ibatis.session.Configuration;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.*;

/**
 * @author Clinton Begin
 */
public class XMLScriptBuilder extends BaseBuilder {

  private final Class<?> parameterType;
  private final Map<String, NodeHandler> nodeHandlerMap = new HashMap<>();
  private final ExpressionEvaluator evaluator = new OgnlExpressionEvaluator();

  public XMLScriptBuilder(Configuration configuration, Class<?> parameterType) {
    super(configuration);
    this.parameterType = parameterType;

    nodeHandlerMap.put("trim", new TrimHandler());
    nodeHandlerMap.put("where", new WhereHandler());
    nodeHandlerMap.put("set", new SetHandler());
    nodeHandlerMap.put("foreach", new ForEachHandler());
    nodeHandlerMap.put("if", new IfHandler(this.evaluator));
    nodeHandlerMap.put("choose", new ChooseHandler());
    nodeHandlerMap.put("when", new IfHandler(this.evaluator));
    nodeHandlerMap.put("otherwise", new OtherwiseHandler());
    nodeHandlerMap.put("bind", new BindHandler(this.evaluator));
    nodeHandlerMap.put("in", new InHandler());
    nodeHandlerMap.put("and", new AndSqlNodeHandler());
  }

  public SqlSource parseScriptNode(XNode context) {
    MixedSqlNode rootSqlNode = parseDynamicTags(context);
    SqlSource sqlSource;
    if (rootSqlNode.isDynamic()) {
      sqlSource = new DynamicSqlSource(rootSqlNode);
    } else {
      String sql = rootSqlNode.buildSql(configuration.createSqlBuildContext(null));
      sqlSource = new RawSqlSource(SqlSourceBuilder.parse(configuration, sql, parameterType, new MapBinding()));
    }
    return sqlSource;
  }

  public MixedSqlNode parseDynamicTags(XNode rootNode) {
    List<SqlNode> contents = new ArrayList<>();
    NodeList children = rootNode.getNode().getChildNodes();

    final int childCount = children.getLength();
    for (int i = 0; i < childCount; i++) {
      XNode child = rootNode.newXNode(children.item(i));
      if (child.getNode().getNodeType() == Node.CDATA_SECTION_NODE || child.getNode().getNodeType() == Node.TEXT_NODE) {
        // the body content of the xml element
        String data = child.getStringBody("");
        if (DynamicCheckerTokenParser.isDynamic(data)) {
          contents.add(new TextSqlNode(evaluator, data, true));
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
      }
    }
    return new MixedSqlNode(contents);
  }

  private static class BindHandler implements NodeHandler {

    ExpressionEvaluator evaluator;

    public BindHandler(ExpressionEvaluator evaluator) {
      // Prevent Synthetic Access
      this.evaluator = evaluator;
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      final String name = nodeToHandle.getStringAttribute(StringKey.NAME);
      final String expression = nodeToHandle.getStringAttribute(StringKey.VALUE);
      final VarDeclSqlNode node = new VarDeclSqlNode(this.evaluator, name, expression);
      targetContents.add(node);
    }
  }

  private class TrimHandler implements NodeHandler {
    public TrimHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      String prefix = nodeToHandle.getStringAttribute(StringKey.PREFIX);
      String prefixOverrides = nodeToHandle.getStringAttribute(StringKey.PREFIX_OVERRIDES);
      String suffix = nodeToHandle.getStringAttribute(StringKey.SUFFIX);
      String suffixOverrides = nodeToHandle.getStringAttribute(StringKey.SUFFIX_OVERRIDES);
      TrimSqlNode trim = new TrimSqlNode(mixedSqlNode, prefix, prefixOverrides, suffix, suffixOverrides);
      targetContents.add(trim);
    }
  }

  private class WhereHandler implements NodeHandler {
    public WhereHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      WhereSqlNode where = new WhereSqlNode(mixedSqlNode);
      targetContents.add(where);
    }
  }

  private class SetHandler implements NodeHandler {
    public SetHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      SetSqlNode set = new SetSqlNode(mixedSqlNode);
      targetContents.add(set);
    }
  }

  private class ForEachHandler implements NodeHandler {
    public ForEachHandler() {
      // Prevent Synthetic Access
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

      nullable = Optional.ofNullable(nullable).orElseGet(configuration::isNullableOnForEach);

      ForEachSqlNode forEachSqlNode = new ForEachSqlNode(mixedSqlNode, collection, nullable, index, item,
        open, close, separator);
      targetContents.add(forEachSqlNode);
    }
  }

  private class IfHandler implements NodeHandler {

    ExpressionEvaluator evaluator;

    public IfHandler(ExpressionEvaluator evaluator) {
      this.evaluator = evaluator;
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      String test = nodeToHandle.getStringAttribute("test");
      IfSqlNode ifSqlNode = new IfSqlNode(this.evaluator, mixedSqlNode, test);
      targetContents.add(ifSqlNode);
    }
  }

  private class OtherwiseHandler implements NodeHandler {
    public OtherwiseHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      MixedSqlNode mixedSqlNode = parseDynamicTags(nodeToHandle);
      targetContents.add(mixedSqlNode);
    }
  }

  private class ChooseHandler implements NodeHandler {
    public ChooseHandler() {
      // Prevent Synthetic Access
    }

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      List<SqlNode> whenSqlNodes = new ArrayList<>();
      List<SqlNode> otherwiseSqlNodes = new ArrayList<>();
      handleWhenOtherwiseNodes(nodeToHandle, whenSqlNodes, otherwiseSqlNodes);
      SqlNode defaultSqlNode = getDefaultSqlNode(otherwiseSqlNodes);
      ChooseSqlNode chooseSqlNode = new ChooseSqlNode(whenSqlNodes, defaultSqlNode);
      targetContents.add(chooseSqlNode);
    }

    private void handleWhenOtherwiseNodes(XNode chooseSqlNode, List<SqlNode> ifSqlNodes,
                                          List<SqlNode> defaultSqlNodes) {
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

  /**
   * @see ForEachHandler
   */
  private class InHandler implements NodeHandler {

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      String collection = nodeToHandle.getStringAttribute(StringKey.COLLECTION);
      String column = nodeToHandle.getStringAttribute(StringKey.COLUMN);
      String item = nodeToHandle.getStringAttribute(StringKey.ITEM);
      String condition = nodeToHandle.getStringAttribute(StringKey.CONDITION);
      Boolean nullable = nodeToHandle.getBooleanAttribute(StringKey.NULLABLE);
      if (nullable == null) {
        nullable = configuration.isNullableOnForEach();
      }

      StaticTextSqlNode contents = new StaticTextSqlNode("#{" + item + "}");
      targetContents.add(new InSqlNode(contents, evaluator, collection, condition, parseItemExpression(item), nullable, column));
    }

    /**
     * item.id -> item
     *
     * @param itemExpression item expression
     * @return item expression value
     */
    private String parseItemExpression(String itemExpression) {
      if (itemExpression == null) {
        return null;
      }
      PropertyTokenizer tokenizer = new PropertyTokenizer(itemExpression);
      if (tokenizer.hasNext()) {
        return tokenizer.getName();
      }
      return itemExpression;
    }
  }

  private class AndSqlNodeHandler implements NodeHandler {

    @Override
    public void handleNode(XNode nodeToHandle, List<SqlNode> targetContents) {
      AndSqlNode andSqlNode = new AndSqlNode(null, null, null);

      System.out.println(targetContents);
    }
  }
}
