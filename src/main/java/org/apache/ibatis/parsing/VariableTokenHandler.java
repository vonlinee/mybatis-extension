package org.apache.ibatis.parsing;

import java.util.Map;

public class VariableTokenHandler implements TokenHandler {

  private final Map<String, String> variables;

  public VariableTokenHandler(Map<String, String> variables) {
    this.variables = variables;
  }

  @Override
  public String handleToken(String content) {
    return variables.get(content);
  }
}
