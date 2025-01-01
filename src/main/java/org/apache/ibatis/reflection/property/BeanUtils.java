/*
 *    Copyright 2009-2023 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection.property;

import org.apache.ibatis.reflection.ReflectionRuntimeException;
import org.apache.ibatis.reflection.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Locale;

/**
 * @author Clinton Begin
 */
public final class BeanUtils {

  static final String BOOLEAN_GETTER_PREFIX = "is";
  static final String GETTER_PREFIX = "get";
  static final String SETTER_PREFIX = "set";

  private BeanUtils() {
    // Prevent Instantiation of Static Class
  }

  public static void copyProperties(Class<?> type, Object sourceBean, Object destinationBean) {
    Class<?> parent = type;
    while (parent != null) {
      final Field[] fields = parent.getDeclaredFields();
      for (Field field : fields) {
        try {
          try {
            field.set(destinationBean, field.get(sourceBean));
          } catch (IllegalAccessException e) {
            if (!ReflectionUtils.canControlMemberAccessible()) {
              throw e;
            }
            field.setAccessible(true);
            field.set(destinationBean, field.get(sourceBean));
          }
        } catch (Exception e) {
          // Nothing useful to do, will only fail on final fields, which will be ignored.
        }
      }
      parent = parent.getSuperclass();
    }
  }

  public static String methodToProperty(String name) {
    if (name.startsWith(BOOLEAN_GETTER_PREFIX)) {
      name = name.substring(2);
    } else if (name.startsWith(GETTER_PREFIX) || name.startsWith(SETTER_PREFIX)) {
      name = name.substring(3);
    } else {
      throw new ReflectionRuntimeException(
        "Error parsing property name '" + name + "'.  Didn't start with 'is', 'get' or 'set'.");
    }
    if (name.length() == 1 || name.length() > 1 && !Character.isUpperCase(name.charAt(1))) {
      name = name.substring(0, 1).toLowerCase(Locale.ENGLISH) + name.substring(1);
    }
    return name;
  }

  public static boolean isGetterMethod(Method method) {
    return isGetter(method.getName());
  }

  public static boolean isBooleanGetterMethod(Method method) {
    return isBooleanGetter(method.getName());
  }

  public static boolean isProperty(String name) {
    return isGetter(name) || isSetter(name);
  }

  public static boolean isBooleanGetter(String name) {
    return name != null && name.startsWith(BOOLEAN_GETTER_PREFIX) && name.length() > 2;
  }

  public static boolean isGetter(String name) {
    return name != null &&
      ((name.startsWith(GETTER_PREFIX) && name.length() > 3)
        || (name.startsWith(BOOLEAN_GETTER_PREFIX) && name.length() > 2));
  }

  public static boolean isSetter(String name) {
    return name.startsWith(SETTER_PREFIX) && name.length() > 3;
  }
}
