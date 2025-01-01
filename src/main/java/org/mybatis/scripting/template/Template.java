package org.mybatis.scripting.template;

import java.io.Writer;

/**
 * 为了统一不同的模板引擎的实现，将各个模板引擎的类进行包装
 */
public interface Template {

  /**
   * 模板名称
   *
   * @return 模板名称，通过此名称来检索已存在的模板
   */
  String getName();

  /**
   * 设置模板名称
   */
  default void setName(String templateName) {
  }

  /**
   * 渲染模板
   *
   * @param dataModel 此模板渲染的参数数据模型，Map或者普通javabean，或者TemplateArgumentsMap
   * @param writer    输出位置
   */
  void render(Object dataModel, Writer writer) throws TemplateException;
}
