package org.apache.ibatis.util;

import java.util.Collection;
import java.util.Map;

/**
 * @author vonlinee
 * @apiNote
 * @since 2025-01-25 13:39
 **/
public final class CollectionUtils {

  /**
   * Null-safe check if the specified collection is empty.
   * <p>
   * Null returns true.
   * </p>
   *
   * @param coll the collection to check, may be null
   * @return true if empty or null
   * @since 3.2
   */
  public static boolean isEmpty(final Collection<?> coll) {
    return coll == null || coll.isEmpty();
  }

  /**
   * Null-safe check if the specified collection is not empty.
   * <p>
   * Null returns false.
   * </p>
   *
   * @param coll the collection to check, may be null
   * @return true if non-null and non-empty
   * @since 3.2
   */
  public static boolean isNotEmpty(final Collection<?> coll) {
    return !isEmpty(coll);
  }

  /**
   * Null-safe check if the specified map is empty.
   * <p>
   * Null returns true.
   * </p>
   *
   * @param map the map to check, may be null
   * @return true if empty or null
   */
  public static boolean isEmpty(final Map<?, ?> map) {
    return map == null || map.isEmpty();
  }

  /**
   * Null-safe check if the specified map is not empty.
   * <p>
   * Null returns false.
   * </p>
   *
   * @param map the collection to check, may be null
   * @return true if non-null and non-empty
   */
  public static boolean isNotEmpty(final Map<?, ?> map) {
    return !isEmpty(map);
  }
}
