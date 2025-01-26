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
package org.apache.ibatis.reflection.factory;

import java.util.List;
import java.util.Properties;

/**
 * MyBatis uses an ObjectFactory to create all needed new Objects.
 * the first ObjectFactory instance will be created by reflection with default no-args constructor.
 * then this instance will be used to create other need object instance
 *
 * @author Clinton Begin
 */
public interface ObjectFactory {

  /**
   * Sets configuration properties.
   *
   * @param properties configuration properties
   */
  default void setProperties(Properties properties) {
    // NOP
  }

  /**
   * get an object with specified type.
   *
   * @param <T>  the generic type
   * @param type Object type
   * @return the t
   */
  <T> T get(Class<T> type);

  /**
   * puts an instance into the registry associated with its class type.
   *
   * @param type     the class type of the instance
   * @param instance the instance to be registered
   */
  void put(Class<?> type, Object instance);

  /**
   * Creates a new object with default constructor.
   *
   * @param <T>  the generic type
   * @param type Object type
   * @return the t
   */
  <T> T create(Class<T> type);

  /**
   * Creates a new object with the specified constructor and params.
   *
   * @param <T>                 the generic type
   * @param type                Object type
   * @param constructorArgTypes Constructor argument types
   * @param constructorArgs     Constructor argument values
   * @return the t
   */
  <T> T create(Class<T> type, List<Class<?>> constructorArgTypes, List<Object> constructorArgs);

  /**
   * Returns true if this object can have a set of other objects. It's main purpose is to support
   * non-java.util.Collection objects like Scala collections.
   *
   * @param <T>  the generic type
   * @param type Object type
   * @return whether it is a collection or not
   * @since 3.1.0
   */
  <T> boolean isCollection(Class<T> type);

}
