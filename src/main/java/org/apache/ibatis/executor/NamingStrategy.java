package org.apache.ibatis.executor;

import org.jetbrains.annotations.Nullable;

/**
 * @author vonlinee
 * @since 2025-01-19 20:31
 **/
public interface NamingStrategy {

  String classToTableName(@Nullable Class<?> type, String className);

  String propertyToColumnName(@Nullable Class<?> type, String columnName);

  String tableNameToClassName(@Nullable Class<?> type, String tableName);

  String columnNameToPropertyName(@Nullable Class<?> type, String tableName);

  NamingStrategy RAW = new NamingStrategy() {
    @Override
    public String classToTableName(@Nullable Class<?> type, String className) {
      return className;
    }

    @Override
    public String propertyToColumnName(@Nullable Class<?> type, String propertyName) {
      return propertyName;
    }

    @Override
    public String tableNameToClassName(@Nullable Class<?> type, String tableName) {
      return tableName;
    }

    @Override
    public String columnNameToPropertyName(@Nullable Class<?> type, String columnName) {
      return columnName;
    }
  };

  NamingStrategy DEFAULT = new NamingStrategy() {
    @Override
    public String classToTableName(@Nullable Class<?> type, String className) {
      return toSnakeCase(className);
    }

    @Override
    public String propertyToColumnName(@Nullable Class<?> type, String property) {
      return toSnakeCase(property);
    }

    @Override
    public String tableNameToClassName(@Nullable Class<?> type, String tableName) {
      return toPascalCase(tableName);
    }

    @Override
    public String columnNameToPropertyName(@Nullable Class<?> type, String column) {
      return toCamelCase(column);
    }
  };

  /**
   * Converts a given camelCase or PascalCase string to snake_case.
   *
   * @param input the input string in camelCase or PascalCase
   * @return the input string converted to snake_case
   */
  static String toSnakeCase(String input) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    StringBuilder result = new StringBuilder();
    for (char ch : input.toCharArray()) {
      if (Character.isUpperCase(ch)) {
        if (result.length() > 0) {
          result.append('_');
        }
        result.append(Character.toLowerCase(ch));
      } else {
        result.append(ch);
      }
    }
    return result.toString();
  }

  /**
   * Converts a given snake_case string to camelCase.
   *
   * @param input the input string in snake_case
   * @return the input string converted to camelCase
   */
  static String toCamelCase(String input) {
    return convertToCamel(input, false);
  }

  /**
   * Converts a given snake_case string to PascalCase.
   *
   * @param input the input string in snake_case
   * @return the input string converted to PascalCase
   */
  static String toPascalCase(String input) {
    return convertToCamel(input, true);
  }

  /**
   * A private helper method to convert snake_case to camelCase or PascalCase.
   *
   * @param input           the input string in snake_case
   * @param capitalizeFirst whether to capitalize the first letter
   * @return the converted string
   */
  static String convertToCamel(String input, boolean capitalizeFirst) {
    if (input == null || input.isEmpty()) {
      return input;
    }
    StringBuilder result = new StringBuilder();
    String[] parts = input.split("_");
    for (int i = 0; i < parts.length; i++) {
      String part = parts[i];
      if (i == 0 && !capitalizeFirst) {
        result.append(part.toLowerCase());
      } else {
        result.append(part.substring(0, 1).toUpperCase());
        result.append(part.substring(1).toLowerCase());
      }
    }
    return result.toString();
  }
}
