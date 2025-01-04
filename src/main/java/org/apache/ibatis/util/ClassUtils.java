package org.apache.ibatis.util;

public final class ClassUtils {

  private ClassUtils() {}

  public static String getPathname(Class<?> type) {
    return type.getName().replace('.', '/');
  }

  public static String getFilename(Class<?> type) {
    return type.getName().replace('.', '/') + ".java";
  }
}
