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

/**
 * Represents a SQL node in a dynamic SQL statement. This interface defines the
 * structure for SQL nodes that can be either static or dynamic in nature.
 *
 * @author Clinton Begin
 */
public interface SqlNode {

  /**
   * @return the name of this SQL node
   */
  String getName();

  /**
   * Checks if this SQL node is dynamic.
   *
   * @return true if the SQL node is dynamic; false otherwise
   */
  boolean isDynamic();

  /**
   * Applies the SQL node to the provided {@link DynamicContext}.
   * This method is responsible for processing the SQL node and updating the context.
   *
   * @param context the {@link DynamicContext} to which this SQL node will be applied
   * @return true if the application was successful; false otherwise
   */
  boolean apply(DynamicContext context);
}
