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

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Clinton Begin
 */
public interface DynamicContext {

  String PARAMETER_OBJECT_KEY = "_parameter";
  String DATABASE_ID_KEY = "_databaseId";

  @NotNull
  Map<String, Object> getBindings();

  void bind(String name, Object value);

  void appendSql(String sql);

  @NotNull
  String getSql();

  int getUniqueNumber();
}
