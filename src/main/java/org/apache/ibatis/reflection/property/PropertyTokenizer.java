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
package org.apache.ibatis.reflection.property;

import java.util.Iterator;

/**
 * @author Clinton Begin
 */
public class PropertyTokenizer implements Iterator<PropertyTokenizer> {

  /**
   * The first block name of the current attribute.
   * for example:
   * <li>if jdbc.name, then name is jdbc</li>
   * <li>if arr[i], then name is arr</li>
   * <li>if username, then name is username</li>
   */
  private String name;

  /**
   * The first block name of the current attribute.
   * for example:
   * <li>if jdbc.name, then name is jdbc</li>
   * <li>if arr[i], then indexedName is arr[i]</li>
   * <li>if username, then name is indexedName</li>
   */
  private final String indexedName;

  /**
   * for example, if full name is arr [1], then index=1
   */
  private String index;

  /**
   * for example: jdbc.name then children is name.
   */
  private final String children;

  public PropertyTokenizer(String fullName) {
    int delimiter = fullName.indexOf('.');
    if (delimiter > -1) {
      name = fullName.substring(0, delimiter);
      children = fullName.substring(delimiter + 1);
    } else {
      name = fullName;
      children = null;
    }
    indexedName = name;
    delimiter = name.indexOf('[');
    if (delimiter > -1) {
      index = name.substring(delimiter + 1, name.length() - 1);
      name = name.substring(0, delimiter);
    }
  }

  public String getName() {
    return name;
  }

  public String getIndex() {
    return index;
  }

  public String getIndexedName() {
    return indexedName;
  }

  public String getChildren() {
    return children;
  }

  @Override
  public boolean hasNext() {
    return children != null;
  }

  @Override
  public PropertyTokenizer next() {
    return new PropertyTokenizer(children);
  }

  @Override
  public void remove() {
    throw new UnsupportedOperationException(
      "Remove is not supported, as it has no meaning in the context of properties.");
  }
}
