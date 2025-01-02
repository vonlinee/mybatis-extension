package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSession;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;

/**
 * 执行默认方法
 */
class DefaultMethodInvoker implements MapperMethodInvoker {
  private final MethodHandle methodHandle;

  public DefaultMethodInvoker(MethodHandle methodHandle) {
    this.methodHandle = methodHandle;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
    return methodHandle.bindTo(proxy).invokeWithArguments(args);
  }
}
