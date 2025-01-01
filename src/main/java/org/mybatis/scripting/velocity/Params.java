package org.mybatis.scripting.velocity;

import static org.mybatis.scripting.velocity.TrimDirective.fromStringList;

public final class Params {

  String prefix = "";

  String suffix = "";

  FastLinkedList<String> prefixOverrides = new FastLinkedList<>();

  FastLinkedList<String> suffixOverrides = new FastLinkedList<>();

  String body = "";

  int maxPrefixLength = 0;

  int maxSuffixLength = 0;

  int maxBody = 0;

  public String getBody() {
    return this.body;
  }

  public void setBody(String value) {
    if (value == null) {
      this.body = "";
    } else {
      this.body = value.trim();
    }
    this.maxBody = this.body.length();
  }

  public String getPrefix() {
    return this.prefix;
  }

  public void setPrefix(String value) {
    this.prefix = value;
  }

  public FastLinkedList<String> getPrefixOverrides() {
    return this.prefixOverrides;
  }

  public void setPrefixOverrides(String list) {
    this.maxPrefixLength = fromStringList(list, '|', this.prefixOverrides);
  }

  public FastLinkedList<String> getSuffixOverrides() {
    return this.suffixOverrides;
  }

  public void setSuffixOverrides(String list) {
    this.maxSuffixLength = fromStringList(list, '|', this.suffixOverrides);
  }

  public String getSuffix() {
    return this.suffix;
  }

  public void setSuffix(String value) {
    this.suffix = value;
  }

}
