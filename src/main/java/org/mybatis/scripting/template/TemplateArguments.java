package org.mybatis.scripting.template;

import java.util.Collections;
import java.util.Map;

/**
 * 模板参数集合，使用Map或者对象
 */
public interface TemplateArguments {

  /**
   * 设置参数值
   *
   * @param name  参数名
   * @param value 参数值
   */
  default void setValue(String name, Object value) {
  }

  /**
   * 添加参数
   *
   * @param name  参数名
   * @param value 参数值
   */
  default void add(String name, Object value) {
    setValue(name, value);
  }

  /**
   * 批量设置
   *
   * @param argumentsMap 参数Map
   */
  default void addAll(Map<String, Object> argumentsMap) {
    for (Map.Entry<String, Object> entry : argumentsMap.entrySet()) {
      add(entry.getKey(), entry.getValue());
    }
  }

  /**
   * 批量设置
   *
   * @param arguments 另一个模板参数集合
   */
  default void addAll(TemplateArguments arguments) {
    addAll(arguments.asMap());
  }

  /**
   * 根据参数名称获取参数值
   *
   * @param name 参数名
   * @return 参数值
   */
  default Object getValue(String name) {
    return null;
  }

  /**
   * 实现类是否是Map
   *
   * @return 默认false
   * @see TemplateArgumentsMap
   */
  default boolean isMap() {
    return false;
  }

  /**
   * 转为Map结构，延迟初始化
   *
   * @return map
   */
  default Map<String, Object> asMap() {
    return Collections.emptyMap();
  }

  /**
   * 填充到另一个模板参数集合
   *
   * @param arguments 另一个模板参数集合
   */
  default void fill(TemplateArguments arguments) {
    if (arguments != null) {
      arguments.addAll(this);
    }
  }

  /**
   * 参数是否为空
   *
   * @return 模板参数集合是否为空
   */
  default boolean isEmpty() {
    if (this instanceof Map<?, ?>) {
      return ((Map<?, ?>) this).isEmpty();
    }
    return false;
  }
}
