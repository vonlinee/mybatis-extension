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

import org.apache.ibatis.scripting.SqlBuildContext;

/**
 * the xml element does not contain any dynamic sql xml tags.
 * Represents a static text SQL node. This class implements the {@link SqlNode} interface
 * and holds a fixed text string that can be applied to a given {@link SqlBuildContext}.
 *
 * @author Clinton Begin
 */
public class StaticTextSqlNode implements SqlNode {

  /**
   * static text, maybe a variable reference like #{user.name}.
   */
  private final String text;

  public StaticTextSqlNode(String text) {
    this.text = text;
  }

  @Override
  public String getName() {
    return "text";
  }

  /**
   * Checks if this SQL node is dynamic.
   *
   * @return false, as StaticTextSqlNode is not dynamic
   */
  @Override
  public final boolean isDynamic() {
    return false;
  }

  /**
   * Applies the static text to the provided {@link SqlBuildContext}.
   *
   * @param context the {@link SqlBuildContext} to which the static text will be appended
   * @return true after applying the static text
   */
  @Override
  public boolean apply(SqlBuildContext context) {
    context.appendSql(text);
    return true;
  }

  public String getText() {
    return text;
  }
}
