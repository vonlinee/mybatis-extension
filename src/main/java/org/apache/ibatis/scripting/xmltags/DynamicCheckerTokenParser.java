package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.parsing.TokenHandler;

public class DynamicCheckerTokenParser implements TokenHandler {

  private boolean isDynamic;

  public DynamicCheckerTokenParser() {
    // Prevent Synthetic Access
  }

  public boolean isDynamic() {
    return isDynamic;
  }

  @Override
  public String handleToken(String content) {
    this.isDynamic = true;
    return null;
  }
}
