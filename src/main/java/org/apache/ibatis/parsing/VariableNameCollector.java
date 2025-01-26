package org.apache.ibatis.parsing;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vonlinee
 * @apiNote
 * @since 2025-01-25 19:27
 **/
public class VariableNameCollector implements TokenHandler {

  private final List<String> vars;

  public VariableNameCollector() {
    this(new ArrayList<>());
  }

  public VariableNameCollector(List<String> vars) {
    this.vars = vars;
  }

  @Override
  public String handleToken(String content) {
    vars.add(content);
    return "";
  }

  public List<String> getVariableNames() {
    return vars;
  }
}
