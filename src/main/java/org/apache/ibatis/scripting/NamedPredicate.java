package org.apache.ibatis.scripting;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a named predicate, which is a condition that can be tested
 * against an object. This interface defines a method to retrieve the
 * name of the predicate and a method to evaluate its condition.
 * used in <if test="condition"></if>
 *
 * @author vonlinee
 * @since 2025-01-22 0:06
 **/
public interface NamedPredicate {

  /**
   * Retrieves the name of the predicate.
   *
   * @return a String representing the name of the predicate
   */
  @NotNull
  String getName();

  /**
   * Tests the specified object against the predicate.
   *
   * @param obj the object to be tested
   * @return true if the object satisfies the predicate;
   * false otherwise
   */
  boolean test(Object obj);

  /**
   * A constant instance of {@link NamedPredicate} that always returns true.
   * This predicate can be used in situations where a condition is always satisfied.
   */
  NamedPredicate TRUE = new NamedPredicate() {
    @Override
    public @NotNull String getName() {
      return "TRUE";
    }

    @Override
    public boolean test(Object obj) {
      return true;
    }
  };

  /**
   * A constant instance of {@link NamedPredicate} that always returns false.
   * This predicate can be used in situations where a condition is always satisfied.
   */
  NamedPredicate FALSE = new NamedPredicate() {
    @Override
    @NotNull
    public String getName() {
      return "FALSE";
    }

    @Override
    public boolean test(Object obj) {
      return false;
    }
  };
}
