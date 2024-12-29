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
package org.apache.ibatis.submitted.result_set_type;

import java.io.Reader;
import java.util.List;
import java.util.function.Function;

import org.apache.ibatis.BaseDataTest;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.ScriptRunner;
import org.apache.ibatis.session.Pagination;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ResultSetTypeTest {

  private static SqlSessionFactory sqlSessionFactory;

  @BeforeAll
  static void setUp() throws Exception {
    // create an SqlSessionFactory
    try (Reader reader = Resources
        .getResourceAsReader("org/apache/ibatis/submitted/result_set_type/mybatis-config.xml")) {
      sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
    }

    // populate in-memory database
    ScriptRunner runner = new ScriptRunner(
        sqlSessionFactory.getConfiguration().getEnvironment().getDataSource().getConnection());
    runner.setDelimiter("go");
    runner.setLogWriter(null);
    runner.setErrorLogWriter(null);
    BaseDataTest.runScript(runner, "org/apache/ibatis/submitted/result_set_type/CreateDB.sql");
  }

  @Test
  void testWithStatement() {
    test(mapper -> mapper.getUserWithStatementAndUnset(Pagination.of(5, 3)), 0);
    test(mapper -> mapper.getUserWithStatementAndDefault(Pagination.of(4, 3)), 1);
    test(mapper -> mapper.getUserWithStatementAndForwardOnly(Pagination.of(3, 3)), 2);
    test(mapper -> mapper.getUserWithStatementAndScrollInsensitive(Pagination.of(2, 2)), 2);
    test(mapper -> mapper.getUserWithStatementAndScrollSensitive(Pagination.of(1, 1)), 1);
  }

  @Test
  void testWithPrepared() {
    test(mapper -> mapper.getUserWithPreparedAndUnset(Pagination.of(5, 3)), 0);
    test(mapper -> mapper.getUserWithPreparedAndDefault(Pagination.of(4, 3)), 1);
    test(mapper -> mapper.getUserWithPreparedAndForwardOnly(Pagination.of(3, 3)), 2);
    test(mapper -> mapper.getUserWithPreparedAndScrollInsensitive(Pagination.of(2, 2)), 2);
    test(mapper -> mapper.getUserWithPreparedAndScrollSensitive(Pagination.of(1, 1)), 1);
  }

  @Test
  void testWithCallable() {
    test(mapper -> mapper.getUserWithCallableAndUnset(Pagination.of(5, 3)), 0);
    test(mapper -> mapper.getUserWithCallableAndDefault(Pagination.of(4, 3)), 1);
    test(mapper -> mapper.getUserWithCallableAndForwardOnly(Pagination.of(3, 3)), 2);
    test(mapper -> mapper.getUserWithCallableAndScrollInsensitive(Pagination.of(2, 2)), 2);
    test(mapper -> mapper.getUserWithCallableAndScrollSensitive(Pagination.of(1, 1)), 1);
  }

  private void test(Function<Mapper, List<User>> usersSupplier, int expectedSize) {
    try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
      Mapper mapper = sqlSession.getMapper(Mapper.class);
      List<User> users = usersSupplier.apply(mapper);
      Assertions.assertEquals(expectedSize, users.size());
    }
  }

}
