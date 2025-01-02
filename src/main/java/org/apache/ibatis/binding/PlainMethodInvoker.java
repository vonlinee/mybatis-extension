package org.apache.ibatis.binding;

import org.apache.ibatis.session.SqlSession;

import java.lang.reflect.Method;

class PlainMethodInvoker implements MapperMethodInvoker {
  private final MapperMethod mapperMethod;

  public PlainMethodInvoker(MapperMethod mapperMethod) {
    this.mapperMethod = mapperMethod;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args, SqlSession sqlSession) throws Throwable {
    return mapperMethod.execute(sqlSession, args);
  }
}
