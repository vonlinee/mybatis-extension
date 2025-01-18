package org.apache.ibatis.binding;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ParamMap {

  Map<String, Object> parameters = new HashMap<>();

  public boolean isEmpty() {
    return parameters.isEmpty();
  }

  public Set<String> keySet() {
    return parameters.keySet();
  }

  public Object get(Object key) {
    if (!parameters.containsKey(key)) {
      throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + parameters.keySet());
    }
    return parameters.get(key);
  }

  public Object put(String key, Object value) {
    return parameters.put(key, value);
  }

  public Map<String, Object> asMap() {
    return parameters;
  }
}
