package org.apache.ibatis.scripting;

/**
 * used in <if test="condition"></if>
 *
 * @author vonlinee
 * @since 2025-01-22 0:06
 **/
public interface NamedPredicate {

  String getName();

  boolean test(Object obj);
}
