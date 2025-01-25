package org.apache.ibatis.scripting;

import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;

public class MapBinding extends AbstractMap<String, Object> implements BindingContext {

  private final Map<String, Object> backingMap;

  public MapBinding(Map<String, Object> backingMap) {
    this.backingMap = backingMap;
  }

  public MapBinding() {
    this(new HashMap<>());
  }

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

  @NotNull
  @Override
  public Set<Entry<String, Object>> entrySet() {
    return backingMap.entrySet();
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

  public MapBinding set(String name, Object value) {
    this.put(name, value);
    return this;
  }

  public static MapBinding of(Map<String, Object> map) {
    return new MapBinding(map);
  }
}
