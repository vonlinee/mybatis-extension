package org.apache.ibatis.parsing;

/**
 * thread not safe
 */
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

  /**
   * TODO refactor this method
   * not contains ${xxx}
   *
   * @param text the text to check
   * @return whether the text is dynamic.
   * @see DynamicCheckerTokenParser#isDynamic(String)
   */
  public static boolean isDynamic(String text) {
    DynamicCheckerTokenParser checker = new DynamicCheckerTokenParser();
    GenericTokenParser parser = new GenericTokenParser("${", "}", checker);
    parser.parse(text);
    return checker.isDynamic();
  }

  /**
   * check the text whether contains token like ${xx} or #{xx}
   *
   * @param text       text
   * @param openToken  open token
   * @param closeToken close token
   * @return whether the text contains token
   * @apiNote return  immediately when the first token is found.
   */
  public static boolean isDynamic(String text, String openToken, String closeToken) {
    if (text == null || text.isEmpty()) {
      return false;
    }
    // search open token
    int start = text.indexOf(openToken);
    if (start == -1) {
      return false;
    }
    char[] src = text.toCharArray();
    int offset;
    do {
      if (start > 0 && src[start - 1] == '\\') {
        // this open token is escaped. continue.
        offset = start + openToken.length();
      } else {
        // found open token. let's search close token.
        offset = start + openToken.length();
        // the first index of close token
        int end = text.indexOf(closeToken, offset);
        while (end > -1) {
          if ((end <= offset) || (src[end - 1] != '\\')) {
            break;
          }
          // this close token is escaped. remove the backslash and continue.
          offset = end + closeToken.length();
          end = text.indexOf(closeToken, offset);
        }
        if (end == -1) {
          // close token was not found.
          offset = src.length;
        } else {
          // found a token
          return true;
        }
      }
      start = text.indexOf(openToken, offset);
    } while (start > -1);
    return false;
  }
}
