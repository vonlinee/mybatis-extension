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
package org.apache.ibatis.mapping;

import org.jetbrains.annotations.Nullable;

/**
 * Represents the content of a mapped statement read from an XML file or an annotation. It creates the SQL that will be
 * passed to the database out of the input parameter received from the user.
 * SqlSource是XML文件或者注解方法中映射语句的实现时表示，通过SqlSourceBuilder.parse()方法创建，
 * SqlSourceBuilder中符号解析器将mybatis中的查询参数#{}转换为?，并记录了参数的顺序。
 *
 * @author Clinton Begin
 * @see org.apache.ibatis.builder.SqlSourceBuilder
 */
public interface SqlSource {

  /**
   * 用于获取映射语句对象的各个组成部分
   *
   * @param parameterObject 参数对象
   * @return BoundSql
   */
  BoundSql getBoundSql(@Nullable Object parameterObject);
}
