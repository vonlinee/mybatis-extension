package org.apache.ibatis.scripting.defaults;

import org.apache.ibatis.scripting.NamedPredicate;
import org.apache.ibatis.scripting.PredicateFactory;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * @author vonlinee
 * @since 2025-01-25 13:12
 **/
public class DefaultPredicateFactory implements PredicateFactory {

  private final Map<String, NamedPredicate> predicateMap;

  private final Map<Class<?>, NamedPredicate> defaultPredicateByTypeMap;

  public DefaultPredicateFactory() {
    predicateMap = new HashMap<>();
    defaultPredicateByTypeMap = new HashMap<>();

    NamedPredicate notNullPredicate = new NamedPredicate() {
      @Override
      @NotNull
      public String getName() {
        return "notNull";
      }

      @Override
      public boolean test(Object obj) {
        return obj != null;
      }
    };
    addPredicate(notNullPredicate);
    defaultPredicateByTypeMap.put(null, notNullPredicate);

    NamedPredicate notEmptyPredicate = new NamedPredicate() {
      @Override
      public @NotNull String getName() {
        return "notEmpty";
      }

      @Override
      public boolean test(Object obj) {
        if (obj == null) {
          return false;
        }
        if (obj instanceof String) {
          return ((String) obj).isEmpty();
        }
        return false;
      }
    };

    addPredicate(notEmptyPredicate);
    defaultPredicateByTypeMap.put(String.class, notEmptyPredicate);
  }

  private void addPredicate(NamedPredicate predicate) {
    if (this.predicateMap.containsKey(predicate.getName())) {
      throw new IllegalArgumentException(
        String.format("An instance with the same name[%s] already exists.", predicate.getName()));
    }
    this.predicateMap.put(predicate.getName(), predicate);
  }

  @Override
  public NamedPredicate get(String name, Class<?> type) {
    NamedPredicate predicate = predicateMap.getOrDefault(name, NamedPredicate.TRUE);
    if (predicate == null) {
      predicate = defaultPredicateByTypeMap.getOrDefault(type, NamedPredicate.TRUE);
    }
    return predicate;
  }
}
