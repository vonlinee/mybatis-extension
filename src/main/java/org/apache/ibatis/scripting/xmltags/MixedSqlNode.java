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
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * Represents a mixed SQL node, which can contain multiple SQL nodes.
 * This class implements the {@link SqlNode} interface and provides functionality
 * to apply all contained SQL nodes to a given {@link SqlBuildContext}.
 *
 * @author Clinton Begin
 */
public class MixedSqlNode implements SqlNode {

  /**
   * the children contents
   */
  @NotNull
  private final List<SqlNode> contents;

  public MixedSqlNode(List<SqlNode> contents) {
    this.contents = Objects.requireNonNull(contents, "contents cannot be null.");
  }

  @Override
  public String getName() {
    return "mixed";
  }

  /**
   * Checks if this SQL node is dynamic.
   *
   * @return false, as MixedSqlNode is not dynamic
   */
  @Override
  public boolean isDynamic() {
    for (SqlNode contentNode : contents) {
      if (contentNode.isDynamic()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Applies the contained SQL nodes to the provided {@link SqlBuildContext}.
   *
   * @param context the {@link SqlBuildContext} to which the contents will be applied
   * @return true after applying all contained nodes
   */
  @Override
  public boolean apply(SqlBuildContext context) {
    contents.forEach(node -> node.apply(context));
    return true;
  }

  @Override
  public boolean hasChildren() {
    return !contents.isEmpty();
  }

  @NotNull
  public List<SqlNode> getChildren() {
    return contents;
  }

  public SqlNode getChild(int i) {
    if (i < 0 || i > contents.size()) {
      return null;
    }
    return contents.get(i);
  }

  public int getChildCount() {
    return contents.size();
  }
}
