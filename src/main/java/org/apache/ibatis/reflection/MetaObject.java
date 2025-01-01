/*
 *    Copyright 2009-2024 the original author or authors.
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
package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * @author Clinton Begin
 */
public interface MetaObject {

  static MetaObject forObject(@Nullable Object object, ObjectFactory objectFactory,
                              ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    if (object == null) {
      return SystemMetaObject.NULL_META_OBJECT;
    }
    return new DefaultMetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  @NotNull
  ObjectWrapper determineObjectWrapper(Object object);

  ObjectFactory getObjectFactory();

  ObjectWrapperFactory getObjectWrapperFactory();

  ReflectorFactory getReflectorFactory();

  Object getOriginalObject();

  String findProperty(@NotNull String propName, boolean useCamelCaseMapping);

  String[] getGetterNames();

  String[] getSetterNames();

  Class<?> getSetterType(@NotNull String name);

  Class<?> getGetterType(@NotNull String name);

  boolean hasSetter(@NotNull String name);

  boolean hasGetter(String name);

  Object getValue(@NotNull String name);

  /**
   * @param name  expression
   * @param value value to set
   */
  void setValue(@NotNull String name, Object value);

  MetaObject metaObjectForProperty(@NotNull String name);

  @NotNull
  ObjectWrapper getObjectWrapper();

  boolean isCollection();

  void add(Object element);

  <E> void addAll(List<E> list);
}
