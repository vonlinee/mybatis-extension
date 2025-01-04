package org.apache.ibatis.reflection;

import org.springframework.lang.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class ReflectionUtils {

  private ReflectionUtils() {
  }

  /**
   * 创建指定类的实例。
   *
   * @param fullQualifiedClassName 要创建实例的类的完全限定名
   * @return 创建的对象实例
   * @throws ReflectionRuntimeException 如果类未找到或实例化失败
   * @throws IllegalArgumentException   参数为空
   */
  public static <T> T instantiate(String fullQualifiedClassName) throws ReflectionRuntimeException {
    if (fullQualifiedClassName == null || fullQualifiedClassName.isBlank()) {
      throw new IllegalArgumentException("type name cannot be null or blank");
    }
    Class<?> type;
    try {
      type = Class.forName(fullQualifiedClassName);
    } catch (ClassNotFoundException e) {
      throw new ReflectionRuntimeException("cannot load class " + fullQualifiedClassName, e);
    }
    @SuppressWarnings("unchecked")
    T instance = (T) instantiate(type);
    return instance;
  }

  /**
   * @param type the type to create
   * @param <T>  object type
   * @return instance
   */
  @SuppressWarnings("unchecked")
  public static <T> T instantiate(Class<T> type) {
    Objects.requireNonNull(type, "type cannot be null");
    try {
      Constructor<?> constructor = type.getConstructor();
      Object instance = constructor.newInstance();
      return (T) instance;
    } catch (NoSuchMethodException e) {
      throw new ReflectionRuntimeException("failed to create instance, cannot find default no-arg constructor in class " + type.getName(), e);
    } catch (InvocationTargetException e) {
      throw new ReflectionRuntimeException(e.getTargetException());
    } catch (InstantiationException e) {
      throw new ReflectionRuntimeException("cannot instantiate from class " + type.getName(), e);
    } catch (IllegalAccessException e) {
      throw new ReflectionRuntimeException("failed to create instance, illegal access.", e);
    }
  }

  /**
   * Make the given method accessible, explicitly setting it accessible if
   * necessary. The {@code setAccessible(true)} method is only called
   * when actually necessary, to avoid unnecessary conflicts.
   *
   * @param method the method to make accessible
   * @see java.lang.reflect.Method#setAccessible
   */
  @SuppressWarnings("deprecation")
  public static void tryMakeAccessible(Method method) {
    try {
      if ((!Modifier.isPublic(method.getModifiers())
        || !Modifier.isPublic(method.getDeclaringClass().getModifiers()))
        && !method.isAccessible()) {
        method.setAccessible(true);
      }
    } catch (Throwable ignored) {
    }
  }

  /**
   * Handle the given reflection exception.
   * <p>Should only be called if no checked exception is expected to be thrown
   * by a target method, or if an error occurs while accessing a method or field.
   * <p>Throws the underlying RuntimeException or Error in case of an
   * InvocationTargetException with such a root cause. Throws an
   * IllegalStateException with an appropriate message or
   * UndeclaredThrowableException otherwise.
   *
   * @param ex the reflection exception to handle
   */
  public static void handleReflectionException(Throwable ex) {
    if (ex instanceof NoSuchMethodException) {
      throw new IllegalStateException("Method not found: " + ex.getMessage());
    }
    if (ex instanceof IllegalAccessException) {
      throw new IllegalStateException("Could not access method or field: " + ex.getMessage());
    }
    if (ex instanceof InvocationTargetException) {
      handleInvocationTargetException((InvocationTargetException) ex);
    }
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    throw new UndeclaredThrowableException(ex);
  }


  /**
   * Handle the given invocation target exception. Should only be called if no
   * checked exception is expected to be thrown by the target method.
   * <p>Throws the underlying RuntimeException or Error in case of such a root
   * cause. Throws an UndeclaredThrowableException otherwise.
   *
   * @param ex the invocation target exception to handle
   */
  public static void handleInvocationTargetException(InvocationTargetException ex) {
    rethrowRuntimeException(ex.getTargetException());
  }

  /**
   * Rethrow the given {@link Throwable exception}, which is presumably the
   * <em>target exception</em> of an {@link InvocationTargetException}.
   * Should only be called if no checked exception is expected to be thrown
   * by the target method.
   * <p>Rethrows the underlying exception cast to a {@link RuntimeException} or
   * {@link Error} if appropriate; otherwise, throws an
   * {@link UndeclaredThrowableException}.
   *
   * @param ex the exception to rethrow
   * @throws RuntimeException the rethrown exception
   */
  public static void rethrowRuntimeException(@Nullable Throwable ex) {
    if (ex instanceof RuntimeException) {
      throw (RuntimeException) ex;
    }
    if (ex instanceof Error) {
      throw (Error) ex;
    }
    throw new UndeclaredThrowableException(ex);
  }

  @SuppressWarnings("unchecked")
  public static <T> T createJdkProxy(Class<?> iType, InvocationHandler handler) {
    Objects.requireNonNull(iType, "type must not be null");
    Objects.requireNonNull(handler, "invocation handler must not be null");
    if (!iType.isInterface()) {
      throw new IllegalArgumentException("type must be an interface");
    }
    return (T) Proxy.newProxyInstance(iType.getClassLoader(), new Class[]{iType}, handler);
  }

  @SuppressWarnings("unchecked")
  public static <T extends InvocationHandler> T getInvocationHandler(Object proxy) {
    return (T) Proxy.getInvocationHandler(proxy);
  }

  public static List<String> getParameterNames(Executable executable) {
    List<String> parameterNames = new ArrayList<>();
    Parameter[] parameters = executable.getParameters();
    for (Parameter parameter : parameters) {
      parameterNames.add(parameter.getName());
    }
    return parameterNames;
  }

  public static List<String> getMethodParamNames(Method method) {
    return getParameterNames(method);
  }

  public static List<String> getParamNames(Constructor<?> constructor) {
    return getParameterNames(constructor);
  }
}
