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
import org.apache.ibatis.reflection.Reflector;

import java.lang.reflect.Field;
import java.util.Locale;

/**
 * @author Clinton Begin
 */
public final class BeanUtils {

  private BeanUtils() {
    // Prevent Instantiation of Static Class
  }

  /**
   * Copies properties from the source bean to the destination bean for the specified type.
   * This method will copy all fields from the source bean to the destination bean,
   * including fields from superclasses.
   * <p>
   * If a field is marked as final, it will be ignored and the method will continue
   * processing other fields without throwing an exception.
   * </p>
   *
   * @param type            the class type of the beans, used to determine the fields to copy
   * @param sourceBean      the source bean from which properties will be copied
   * @param destinationBean the destination bean to which properties will be set
   * @throws IllegalArgumentException if either sourceBean or destinationBean is null
   * @throws SecurityException        if access to a field is denied
   */
  public static void copyProperties(Class<?> type, Object sourceBean, Object destinationBean) {
    Class<?> parent = type;
    while (parent != null) {
      final Field[] fields = parent.getDeclaredFields();
      for (Field field : fields) {
        try {
          try {
            field.set(destinationBean, field.get(sourceBean));
          } catch (IllegalAccessException e) {
            if (!Reflector.canControlMemberAccessible()) {
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

  /**
   * Converts a method name to its corresponding property name following JavaBeans conventions.
   * <p>
   * This method handles method names that start with "is", "get", or "set". It removes the
   * prefix and transforms the first character of the property name to lowercase if necessary.
   * </p>
   *
   * @param name the method name to be converted to a property name
   * @return the corresponding property name
   * @throws ReflectionRuntimeException if the method name does not start with "is", "get", or "set"
   */
  public static String methodToProperty(String name) {
    if (name.startsWith("is")) {
      name = name.substring(2);
    } else if (name.startsWith("get") || name.startsWith("set")) {
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

  /**
   * Determines if the given method name is a valid property name,
   * which includes both getter and setter methods.
   *
   * @param name the method name to check
   * @return {@code true} if the name is a getter or setter; {@code false} otherwise
   */
  public static boolean isProperty(String name) {
    return isGetter(name) || isSetter(name);
  }

  /**
   * Checks if the given method name is a valid getter method name.
   * A method is considered a getter if it starts with "get" and has more than three characters,
   * or if it starts with "is" and has more than two characters.
   *
   * @param name the method name to check
   * @return {@code true} if the name is a getter; {@code false} otherwise
   */
  public static boolean isGetter(String name) {
    return name.startsWith("get") && name.length() > 3 || name.startsWith("is") && name.length() > 2;
  }

  /**
   * Checks if the given method name is a valid setter method name.
   * A method is considered a setter if it starts with "set" and has more than three characters.
   *
   * @param name the method name to check
   * @return {@code true} if the name is a setter; {@code false} otherwise
   */
  public static boolean isSetter(String name) {
    return name.startsWith("set") && name.length() > 3;
  }
}
