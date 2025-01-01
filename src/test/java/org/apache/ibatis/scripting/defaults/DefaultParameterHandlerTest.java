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
package org.apache.ibatis.scripting.defaults;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.domain.blog.Author;
import org.apache.ibatis.domain.blog.Section;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.mapping.ResultMapping;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.scripting.xmltags.XMLLanguageDriver;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeException;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

/**
 * DefaultParameterHandlerTest
 *
 * @author Ryan Lamore
 */
class DefaultParameterHandlerTest {

  @Test
  void setParametersThrowsProperException() throws SQLException {
    final MappedStatement mappedStatement = getMappedStatement();
    final BoundSql boundSql = mock(BoundSql.class);

    TypeHandler<?> typeHandler = mock(TypeHandler.class);
    doThrow(new SQLException("foo")).when(typeHandler).setParameter(any(PreparedStatement.class), anyInt(), any(),
      any(JdbcType.class));
    ParameterMapping parameterMapping = new ParameterMapping.Builder("prop",
      typeHandler).build(mappedStatement.getConfiguration());
    List<ParameterMapping> parameterMappings = Collections.singletonList(parameterMapping);
    when(boundSql.getParameterMappings()).thenReturn(parameterMappings);

    DefaultParameterHandler defaultParameterHandler = new DefaultParameterHandler(mappedStatement, null,
      boundSql);

    PreparedStatement ps = mock(PreparedStatement.class);
    try {
      defaultParameterHandler.setParameters(ps, mappedStatement, boundSql);
      Assertions.fail("Should have thrown TypeException");
    } catch (Exception e) {
      Assertions.assertInstanceOf(TypeException.class, e, "expected TypeException");
      Assertions.assertTrue(e.getMessage().contains("mapping: ParameterMapping"));
    }

  }

  MappedStatement getMappedStatement() {
    final Configuration config = new Configuration();
    final TypeHandlerRegistry registry = config.getTypeHandlerRegistry();
    return new MappedStatement.Builder(config, "testSelect", new StaticSqlSource(config, "some select statement"),
      SqlCommandType.SELECT).resultMaps(new ArrayList<>() {
      private static final long serialVersionUID = 1L;

      {
        add(new ResultMap.Builder(config, "testMap", HashMap.class, new ArrayList<>() {
          {
            add(new ResultMapping.Builder("cOlUmN1", "CoLuMn1", registry.getTypeHandler(Integer.class))
              .build(config));
          }
        }).build());
      }
    }).build();
  }

  @Test
  void testParameterObjectGetPropertyValueWithAdditionalParameter() throws SQLException {
    Configuration config = new Configuration();
    TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

    MappedStatement mappedStatement = new MappedStatement.Builder(config, "testSelect",
      new StaticSqlSource(config, "some select statement"), SqlCommandType.SELECT).build();

    Object parameterObject = 1;

    BoundSql boundSql = new BoundSql(config, "some select statement", new ArrayList<>() {
      {
        add(new ParameterMapping.Builder("id", registry.getTypeHandler(int.class)).build(config));
      }
    }, parameterObject) {
      {
        setAdditionalParameter("id", 2);
      }
    };

    DefaultParameterHandler defaultParameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject,
      boundSql);

    PreparedStatement ps = mock(PreparedStatement.class);

    defaultParameterHandler.setParameters(ps, mappedStatement, boundSql);

    verify(ps, times(1)).setInt(1, 2);
  }

  @Test
  void testParameterObjectGetPropertyValueWithNull() throws SQLException {
    Configuration config = new Configuration();
    TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

    MappedStatement mappedStatement = new MappedStatement.Builder(config, "testSelect",
      new StaticSqlSource(config, "some select statement"), SqlCommandType.SELECT).build();

    BoundSql boundSql = new BoundSql(config, "some select statement", new ArrayList<>() {
      {
        add(new ParameterMapping.Builder("id", registry.getTypeHandler(int.class)).build(config));
      }
    }, null);

    DefaultParameterHandler defaultParameterHandler = new DefaultParameterHandler(mappedStatement, null,
      boundSql);

    PreparedStatement ps = mock(PreparedStatement.class);

    defaultParameterHandler.setParameters(ps, mappedStatement, boundSql);

    verify(ps, times(1)).setNull(1, config.getJdbcTypeForNull().TYPE_CODE);
  }

  @Test
  void testParameterObjectGetPropertyValueWithTypeHandler() throws SQLException {
    Configuration config = new Configuration();
    TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

    MappedStatement mappedStatement = new MappedStatement.Builder(config, "testSelect",
      new StaticSqlSource(config, "some select statement"), SqlCommandType.SELECT).build();

    Integer parameterObject = 1;

    BoundSql boundSql = new BoundSql(config, "some select statement", new ArrayList<>() {
      {
        add(new ParameterMapping.Builder("id", registry.getTypeHandler(int.class)).build(config));
      }
    }, parameterObject);

    DefaultParameterHandler defaultParameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject,
      boundSql);

    PreparedStatement ps = mock(PreparedStatement.class);

    defaultParameterHandler.setParameters(ps, mappedStatement, boundSql);

    verify(ps, times(1)).setInt(1, parameterObject);
  }

  @Test
  void testParameterObjectGetPropertyValueWithMetaObject() throws SQLException {
    Configuration config = new Configuration();
    TypeHandlerRegistry registry = config.getTypeHandlerRegistry();

    MappedStatement mappedStatement = new MappedStatement.Builder(config, "testSelect",
      new StaticSqlSource(config, "some select statement"), SqlCommandType.SELECT).build();

    Author parameterObject = new Author(-1, "cbegin", "******", "cbegin@nowhere.com", "N/A", Section.NEWS);

    BoundSql boundSql = new BoundSql(config, "some select statement", new ArrayList<>() {
      {
        add(new ParameterMapping.Builder("id", registry.getTypeHandler(int.class)).build(config));
        add(new ParameterMapping.Builder("username", registry.getTypeHandler(String.class)).build(config));
        add(new ParameterMapping.Builder("password", registry.getTypeHandler(String.class)).build(config));
        add(new ParameterMapping.Builder("email", registry.getTypeHandler(String.class)).build(config));
        add(new ParameterMapping.Builder("bio", registry.getTypeHandler(String.class))
          .jdbcType(JdbcType.VARCHAR).build(config));
        add(new ParameterMapping.Builder("favouriteSection", registry.getTypeHandler(Section.class))
          .jdbcType(JdbcType.VARCHAR).build(config));
      }
    }, parameterObject);

    DefaultParameterHandler defaultParameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject,
      boundSql);

    PreparedStatement ps = mock(PreparedStatement.class);

    defaultParameterHandler.setParameters(ps, mappedStatement, boundSql);

    verify(ps, times(1)).setInt(1, parameterObject.getId());
    verify(ps, times(1)).setString(2, parameterObject.getUsername());
    verify(ps, times(1)).setString(3, parameterObject.getPassword());
    verify(ps, times(1)).setString(4, parameterObject.getEmail());
    verify(ps, times(1)).setString(5, parameterObject.getBio());
    verify(ps, times(1)).setObject(6, parameterObject.getFavouriteSection().name(), JdbcType.VARCHAR.TYPE_CODE);
  }

  @Test
  void testParameterObjectGetPropertyValueWithMetaObjectAndCreateOnce() {
    Author parameterObject = mock(Author.class);

    Configuration mockConfig = mock(Configuration.class);

    final ObjectFactory objectFactory = new DefaultObjectFactory();
    final ObjectWrapperFactory objectWrapperFactory = new DefaultObjectWrapperFactory();
    final ReflectorFactory reflectorFactory = new DefaultReflectorFactory();

    when(mockConfig.getTypeHandlerRegistry()).thenReturn(new TypeHandlerRegistry(mockConfig));
    when(mockConfig.getDefaultScriptingLanguageInstance()).thenReturn(new XMLLanguageDriver());
    when(mockConfig.newMetaObject(parameterObject))
      .thenReturn(MetaObject.forObject(parameterObject, objectFactory, objectWrapperFactory, reflectorFactory));

    TypeHandlerRegistry registry = mockConfig.getTypeHandlerRegistry();

    MappedStatement mappedStatement = new MappedStatement.Builder(mockConfig, "testSelect",
      new StaticSqlSource(mockConfig, "some select statement"), SqlCommandType.SELECT).build();

    BoundSql boundSql = new BoundSql(mockConfig, "some select statement", new ArrayList<>() {
      {
        add(new ParameterMapping.Builder("id", registry.getTypeHandler(int.class))
          .jdbcType(JdbcType.INTEGER).build(mockConfig));
        add(new ParameterMapping.Builder("username", registry.getTypeHandler(String.class))
          .jdbcType(JdbcType.VARCHAR).build(mockConfig));
        add(new ParameterMapping.Builder("password", registry.getTypeHandler(String.class))
          .jdbcType(JdbcType.VARCHAR).build(mockConfig));
        add(new ParameterMapping.Builder("email", registry.getTypeHandler(String.class))
          .jdbcType(JdbcType.VARCHAR).build(mockConfig));
        add(new ParameterMapping.Builder("bio", registry.getTypeHandler(String.class))
          .jdbcType(JdbcType.VARCHAR).build(mockConfig));
        add(new ParameterMapping.Builder("favouriteSection", registry.getTypeHandler(Section.class))
          .jdbcType(JdbcType.VARCHAR).build(mockConfig));
      }
    }, parameterObject);

    DefaultParameterHandler defaultParameterHandler = new DefaultParameterHandler(mappedStatement, parameterObject,
      boundSql);

    PreparedStatement ps = mock(PreparedStatement.class);

    defaultParameterHandler.setParameters(ps, mappedStatement, boundSql);

    verify(parameterObject, times(1)).getId();
    verify(parameterObject, times(1)).getUsername();
    verify(parameterObject, times(1)).getPassword();
    verify(parameterObject, times(1)).getEmail();
    verify(parameterObject, times(1)).getBio();
    verify(parameterObject, times(1)).getFavouriteSection();

    verify(mockConfig, times(1)).newMetaObject(parameterObject);
  }
}
