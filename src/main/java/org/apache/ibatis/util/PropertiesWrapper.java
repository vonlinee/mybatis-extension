package org.apache.ibatis.util;

import org.jetbrains.annotations.NotNull;

import java.util.Properties;

public final class PropertiesWrapper {

  @NotNull
  private final Properties properties;

  public PropertiesWrapper(@NotNull Properties properties) {
    this.properties = properties;
  }

  public Integer getInteger(String key, Integer defaultValue) {
    String value = properties.getProperty(key);
    if (value == null) {
      return defaultValue;
    }
    return Integer.valueOf(value);
  }

  public String getString(String key, String defaultValue) {
    return properties.getProperty(key, defaultValue);
  }

  public Boolean getBoolean(String key, Boolean defaultValue) {
    String value = properties.getProperty(key);
    return value == null ? defaultValue : Boolean.valueOf(value);
  }

  public <E extends Enum<E>> E getEnum(String key, Class<E> enumType, String defaultValue) {
    String value = properties.getProperty(key);
    if (value == null) {
      value = defaultValue;
    }
    return Enum.valueOf(enumType, value);
  }

  public <E extends Enum<E>> E getEnum(String key, Class<E> enumType, E defaultValue) {
    String value = properties.getProperty(key);
    if (value == null) {
      return defaultValue;
    }
    return Enum.valueOf(enumType, value);
  }
}
