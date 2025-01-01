package org.apache.ibatis.reflection;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.CollectionWrapper;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

class DefaultMetaObject implements MetaObject {

  /**
   * the original object
   */
  private final Object originalObject;

  @NotNull
  private final ObjectWrapper objectWrapper;
  private final ObjectFactory objectFactory;
  private final ObjectWrapperFactory objectWrapperFactory;
  private final ReflectorFactory reflectorFactory;

  DefaultMetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.originalObject = object;
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;
    this.objectWrapper = determineObjectWrapper(object);
  }

  @Override
  @NotNull
  @SuppressWarnings({"rawtypes", "unchecked"})
  public ObjectWrapper determineObjectWrapper(Object object) {
    ObjectWrapper wrapper;
    if (object instanceof ObjectWrapper) {
      wrapper = (ObjectWrapper) object;
    } else if (objectWrapperFactory.hasWrapperFor(object)) {
      wrapper = objectWrapperFactory.getWrapperFor(this, object);
    } else if (object instanceof Map) {
      wrapper = new MapWrapper(this, (Map) object);
    } else if (object instanceof Collection) {
      wrapper = new CollectionWrapper((Collection) object);
    } else {
      wrapper = new BeanWrapper(this, object);
    }
    return wrapper;
  }

  public static MetaObject forObject(@Nullable Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    if (object == null) {
      return SystemMetaObject.NULL_META_OBJECT;
    }
    return new DefaultMetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  @Override
  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  @Override
  public ObjectWrapperFactory getObjectWrapperFactory() {
    return objectWrapperFactory;
  }

  @Override
  public ReflectorFactory getReflectorFactory() {
    return reflectorFactory;
  }

  @Override
  public Object getOriginalObject() {
    return originalObject;
  }

  @Override
  public String findProperty(@NotNull String propName, boolean useCamelCaseMapping) {
    return objectWrapper.findProperty(propName, useCamelCaseMapping);
  }

  @Override
  public String[] getGetterNames() {
    return objectWrapper.getGetterNames();
  }

  @Override
  public String[] getSetterNames() {
    return objectWrapper.getSetterNames();
  }

  @Override
  public Class<?> getSetterType(@NotNull String name) {
    return objectWrapper.getSetterType(name);
  }

  @Override
  public Class<?> getGetterType(@NotNull String name) {
    return objectWrapper.getGetterType(name);
  }

  @Override
  public boolean hasSetter(@NotNull String name) {
    return objectWrapper.hasSetter(name);
  }

  @Override
  public boolean hasGetter(String name) {
    return objectWrapper.hasGetter(name);
  }

  @Override
  public Object getValue(@NotNull String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    return objectWrapper.get(prop);
  }

  @Override
  public void setValue(@NotNull String name, Object value) {
    objectWrapper.set(new PropertyTokenizer(name), value);
  }

  @Override
  public MetaObject metaObjectForProperty(@NotNull String name) {
    Object value = getValue(name);
    return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  @NotNull
  @Override
  public ObjectWrapper getObjectWrapper() {
    return objectWrapper;
  }

  @Override
  public boolean isCollection() {
    return objectWrapper.isCollection();
  }

  @Override
  public void add(@NotNull Object element) {
    objectWrapper.add(element);
  }

  @Override
  public <E> void addAll(@NotNull List<E> list) {
    objectWrapper.addAll(list);
  }
}
