package org.apache.ibatis.session.defaults;

import org.apache.ibatis.binding.BindingException;

import java.util.HashMap;

public class StrictMap<V> extends HashMap<String, V> {

  private static final long serialVersionUID = -5741767162221585340L;

  @Override
  public V get(Object key) {
    if (!super.containsKey(key)) {
      throw new BindingException("Parameter '" + key + "' not found. Available parameters are " + this.keySet());
    }
    return super.get(key);
  }

}
