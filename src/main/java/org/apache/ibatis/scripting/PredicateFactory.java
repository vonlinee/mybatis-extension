package org.apache.ibatis.scripting;

/**
 * @author vonlinee
 * @since 2025-01-22 0:05
 **/
public interface PredicateFactory {

  NamedPredicate get(Class<?> type);
}
