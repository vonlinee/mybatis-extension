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
package org.apache.ibatis.executor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.transaction.Transaction;
import org.jetbrains.annotations.NotNull;

import java.sql.SQLException;
import java.util.List;

/**
 * @author Clinton Begin
 */
public interface Executor {

  ResultHandler<?> NO_RESULT_HANDLER = null;

  int update(@NotNull MappedStatement ms, Object parameter) throws SQLException;

  <E> List<E> query(@NotNull MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler<?> resultHandler,
                    CacheKey cacheKey, BoundSql boundSql) throws SQLException;

  <E> List<E> query(@NotNull MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler<?> resultHandler)
    throws SQLException;

  <E> Cursor<E> queryCursor(@NotNull MappedStatement ms, Object parameter, RowBounds rowBounds) throws SQLException;

  List<BatchResult> flushStatements() throws SQLException;

  /**
   * 提交事务
   */
  void commit(boolean required) throws SQLException;

  void rollback(boolean required) throws SQLException;

  CacheKey createCacheKey(@NotNull MappedStatement ms, Object parameterObject, RowBounds rowBounds, BoundSql boundSql);

  boolean isCached(@NotNull MappedStatement ms, CacheKey key);

  void clearLocalCache();

  /**
   * 嵌套查询, 延迟加载
   */
  void deferLoad(@NotNull MappedStatement ms, MetaObject resultObject, String property, CacheKey key, Class<?> targetType);

  /**
   * 事务管理
   *
   * @return 事务管理实现
   */
  Transaction getTransaction();

  /**
   * 关闭操作
   *
   * @param forceRollback 是否强制回滚
   */
  void close(boolean forceRollback);

  /**
   * 当前Executor是否已经关闭
   *
   * @return Executor是否已经关闭
   */
  boolean isClosed();

  void setExecutorWrapper(Executor executor);

}
