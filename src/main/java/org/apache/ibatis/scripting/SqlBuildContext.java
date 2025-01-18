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
package org.apache.ibatis.scripting;

/**
 * @author Clinton Begin
 */
public interface SqlBuildContext {

  String PARAMETER_OBJECT_KEY = "_parameter";
  String DATABASE_ID_KEY = "_databaseId";

  /**
   * Sets the database identifier for the current context.
   *
   * @param databaseId the identifier of the database
   */
  void setDatabaseId(String databaseId);

  /**
   * Retrieves the database identifier for the current context.
   *
   * @return the database identifier
   */
  String getDatabaseId();

  /**
   * Retrieves the current binding context, which holds the parameter bindings.
   *
   * @return the binding context
   */
  BindingContext getBindings();

  /**
   * Binds a value to a given name in the current context.
   *
   * @param name  the name to bind the value to
   * @param value the value to bind
   */
  void bind(String name, Object value);

  /**
   * Appends a SQL fragment to the current SQL statement being built.
   *
   * @param sql the SQL fragment to append
   */
  void appendSql(String sql);

  /**
   * Retrieves the complete SQL statement constructed so far.
   *
   * @return the constructed SQL statement
   */
  String getSql();

  /**
   * Retrieves a unique number that may be used to identify this context instance.
   *
   * @return a unique number
   */
  int getUniqueNumber();
}
