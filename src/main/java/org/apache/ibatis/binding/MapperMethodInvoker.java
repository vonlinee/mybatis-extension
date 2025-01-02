package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

public interface MapperMethodInvoker {

  /**
   * @param proxy      proxy
   * @param method     method
   * @param args       method arguments
   * @param sqlSession SqlSession
   * @return return value
   * @throws Throwable failed to invoke
   */
  Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable;
}
