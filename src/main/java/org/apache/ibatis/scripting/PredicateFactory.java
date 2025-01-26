package org.apache.ibatis.scripting;

/**
 * A factory interface for creating named predicates based on a specified type.
 * Implementations of this interface should provide a way to retrieve
 * a {@link NamedPredicate} associated with a particular class type.
 *
 * @author vonlinee
 * @since 2025-01-22 0:05
 */
public interface PredicateFactory {

  /**
   * Retrieves a {@link NamedPredicate} for the specified class type.
   *
   * @param valueType the class type for which to retrieve a named predicate
   * @return a {@link NamedPredicate} associated with the specified type;
   * returns null if no predicate is found for the type
   */
  NamedPredicate get(String name, Class<?> valueType);
}
