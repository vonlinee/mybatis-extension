package org.mybatis.scripting.template;

import java.io.Writer;
import java.util.Properties;

/**
 * 模板引擎实现
 *
 * @see Template
 * @see TemplateException 所有异常通过TemplateException进行抛出
 */
public interface TemplateEngine {

  /**
   * 设置属性
   *
   * @param properties 配置参数
   */
  default void setProperties(Properties properties) {
  }

  /**
   * 渲染字符串模板
   *
   * @param template  模板内容，不能为null或者空
   * @param arguments 模板参数
   * @param writer    渲染结果
   */
  void evaluate(String template, Object arguments, Writer writer) throws TemplateException;

  /**
   * 加载模板
   *
   * @param name     模板名称
   * @param template 模板唯一ID，或者模板内容
   * @return Template
   */
  Template getTemplate(String name, String template) throws TemplateException;
}
