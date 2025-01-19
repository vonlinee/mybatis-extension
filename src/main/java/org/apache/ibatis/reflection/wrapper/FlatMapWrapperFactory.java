package org.apache.ibatis.reflection.wrapper;

import org.apache.ibatis.reflection.MetaObject;

import java.util.Map;

public class FlatMapWrapperFactory implements ObjectWrapperFactory {
  @Override
  public boolean hasWrapperFor(Object object) {
    return object instanceof Map;
  }

  @SuppressWarnings("unchecked")
  @Override
  public ObjectWrapper getWrapperFor(MetaObject metaObject, Object object) {
    return new FlatMapWrapper(metaObject, (Map<String, Object>) object, metaObject.getObjectFactory());
  }
}
