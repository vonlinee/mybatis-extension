package org.mybatis.scripting.template;

import java.util.HashMap;
import java.util.Map;

/**
 * 模板参数Map, 等同于Map<String, Object>
 */
public final class TemplateArgumentsMap implements TemplateArguments {

  private final Map<String, Object> argumentsMap;

  public TemplateArgumentsMap() {
    this.argumentsMap = new HashMap<>();
  }

  public TemplateArgumentsMap(Map<? extends String, ?> m) {
    this(m, false);
  }

  /**
   * @param m      参数Map
   * @param create 是否创建一个新Map
   */
  @SuppressWarnings("unchecked")
  public TemplateArgumentsMap(Map<? extends String, ?> m, boolean create) {
    this.argumentsMap = create ? new HashMap<>(m) : (Map<String, Object>) m;
  }

  @Override
  public Map<String, Object> asMap() {
    return argumentsMap;
  }

  @Override
  public void setValue(String name, Object value) {
    argumentsMap.put(name, value);
  }

  @Override
  public Object getValue(String name) {
    return argumentsMap.get(name);
  }

  @Override
  public boolean isMap() {
    return true;
  }
}
