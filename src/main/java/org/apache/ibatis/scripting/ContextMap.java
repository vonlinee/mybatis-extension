package org.apache.ibatis.scripting;

import org.apache.ibatis.reflection.MetaObject;

import java.util.HashMap;
import java.util.function.BiConsumer;

public class ContextMap extends HashMap<String, Object> implements BindingContext {

  private final MetaObject parameterMetaObject;
  private final boolean fallbackParameterObject;

  public ContextMap(MetaObject parameterMetaObject, boolean fallbackParameterObject) {
    this.parameterMetaObject = parameterMetaObject;
    this.fallbackParameterObject = fallbackParameterObject;
  }

  @Override
  public boolean containsKey(Object key) {
    String strKey = (String) key;
    if (super.containsKey(strKey)) {
      return super.containsKey(strKey);
    }
    return false;
  }

  @Override
  public Object get(Object key) {
    String strKey = (String) key;
    if (super.containsKey(strKey)) {
      return super.get(strKey);
    }

    if (parameterMetaObject == null) {
      return null;
    }

    if (fallbackParameterObject && !parameterMetaObject.hasGetter(strKey)) {
      return parameterMetaObject.getOriginalObject();
    }
    // issue #61 do not modify the context when reading
    return parameterMetaObject.getValue(strKey);
  }

  @Override
  public void iterateFor(BiConsumer<String, Object> consumer) {
    this.forEach(consumer);
  }

  @Override
  public void remove(String key) {
    super.remove(key);
  }
}
