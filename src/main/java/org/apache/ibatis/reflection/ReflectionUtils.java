package org.apache.ibatis.reflection;

import org.jetbrains.annotations.Nullable;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.lang.reflect.ReflectPermission;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public final class ReflectionUtils {

  @Nullable
  private static final MethodHandle isRecordMethodHandle = getIsRecordMethodHandle();

  public static MethodHandle getIsRecordMethodHandle() {
    MethodHandles.Lookup lookup = MethodHandles.lookup();
    MethodType mt = MethodType.methodType(boolean.class);
    try {
      return lookup.findVirtual(Class.class, "isRecord", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      return null;
    }
  }

  /**
   * Class.isRecord() alternative for Java 15 and older.
   */
  public static boolean isRecordType(Class<?> clazz) {
    try {
      return isRecordMethodHandle != null && (boolean) isRecordMethodHandle.invokeExact(clazz);
    } catch (Throwable e) {
      throw new ReflectionRuntimeException("Failed to invoke 'Class.isRecord()'.", e);
    }
  }

  /**
   * Checks whether you can control member accessible.
   *
   * @return If you can control member accessible, it return {@literal true}
   * @since 3.5.0
   */
  public static boolean canControlMemberAccessible() {
    try {
      SecurityManager securityManager = System.getSecurityManager();
      if (null != securityManager) {
        securityManager.checkPermission(new ReflectPermission("suppressAccessChecks"));
      }
    } catch (SecurityException e) {
      return false;
    }
    return true;
  }

  public static List<String> getParamNames(Method method) {
    return getParameterNames(method);
  }

  public static List<String> getParamNames(Constructor<?> constructor) {
    return getParameterNames(constructor);
  }

  private static List<String> getParameterNames(Executable executable) {
    return Arrays.stream(executable.getParameters()).map(Parameter::getName).collect(Collectors.toList());
  }

  @SuppressWarnings("unchecked")
  public static <T> T createJdkProxy(Class<T> interfaceClass, InvocationHandler invocationHandler) {
    return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class[]{interfaceClass}, invocationHandler);
  }

  @SuppressWarnings("unchecked")
  public static <T> T createJdkProxy(ClassLoader classLoader, Class<T> interfaceClass, InvocationHandler invocationHandler) {
    return (T) Proxy.newProxyInstance(classLoader, new Class[]{interfaceClass}, invocationHandler);
  }
}
