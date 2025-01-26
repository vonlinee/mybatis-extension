package org.apache.ibatis.util;

/**
 * Utility class for operations related to Java class types.
 * This class provides methods to retrieve the pathname and filename
 * associated with a given class type.
 */
public final class ClassUtils {

  private ClassUtils() {
  }

  /**
   * Retrieves the pathname of the specified class type.
   * The pathname is the fully qualified name of the class
   * with dots (.) replaced by slashes (/).
   *
   * @param type the class type for which to retrieve the pathname
   * @return a String representing the pathname of the class
   */
  public static String getPathname(Class<?> type) {
    // Replace dots with slashes to convert the class name to a pathname
    return type.getName().replace('.', '/');
  }

  /**
   * Retrieves the filename of the specified class type.
   * The filename is the fully qualified name of the class
   * with dots (.) replaced by slashes (/), followed by the ".java" extension.
   *
   * @param type the class type for which to retrieve the filename
   * @return a String representing the filename of the class
   */
  public static String getFilename(Class<?> type) {
    // Create the filename by appending ".java" to the pathname
    return type.getName().replace('.', '/') + ".java";
  }
}
