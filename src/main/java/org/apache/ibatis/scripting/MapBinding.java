package org.apache.ibatis.scripting;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class MapBinding implements BindingContext {

  private final Map<String, Object> backingMap = new HashMap<>();

  @Override
  public boolean containsKey(Object key) {
    if (key instanceof String) {
      return backingMap.containsKey(key);
    }
    return false;
  }

  @Override
  public Object get(Object key) {
    if (key instanceof String) {
      return backingMap.get(key);
    }
    throw new IllegalArgumentException("the type of key int current context must be a string.");
  }

  @Override
  public Object put(String key, Object value) {
    return backingMap.put(key, value);
  }

  @Override
  public void iterateFor(BiConsumer<String, Object> consumer) {
    backingMap.forEach(consumer);
  }

  @Override
  public void remove(String key) {
    backingMap.remove(key);
  }

  @Override
  public Map<String, Object> asMap() {
    return backingMap;
  }
}
