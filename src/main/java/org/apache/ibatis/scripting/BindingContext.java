package org.apache.ibatis.scripting;

import java.util.Map;
import java.util.function.BiConsumer;

/**
 * Represents a context for binding values by key.
 */
public interface BindingContext {

  /**
   * Checks whether the context contains a mapping for the specified key.
   *
   * @param key the key whose presence in the context is to be tested
   * @return true if the context contains a mapping for the specified key; false otherwise
   */
  boolean containsKey(Object key);

  /**
   * Retrieves the value associated with the specified key.
   *
   * @param key the key whose associated value is to be returned
   * @return the value associated with the specified key, or null if no value is found
   */
  Object get(Object key);

  /**
   * Associates the specified value with the specified key in this context.
   *
   * @param key   the key with which the specified value is to be associated
   * @param value the value to be associated with the specified key
   * @return the previous value associated with the key, or null if there was no mapping for the key
   */
  Object put(String key, Object value);

  /**
   * Iterates over a specified range of key-value pairs in the context and performs the given action.
   *
   * @param consumer a function that accepts a key and a value, and performs an action on them
   */
  void iterateFor(BiConsumer<String, Object> consumer);

  /**
   * Removes the key-value pair associated with the specified key from the context.
   *
   * @param key the key of the key-value pair to be removed
   */
  void remove(String key);

  /**
   * Removes the key-value pair associated with the specified key from the context.
   *
   * @param keys the key of the key-value pair to be removed
   */
  default void removeKeys(String... keys) {
    if (keys == null) {
      return;
    }
    for (String key : keys) {
      remove(key);
    }
  }

  /**
   * for compatibility
   *
   * @return context as map
   */
  Map<String, Object> asMap();
}
