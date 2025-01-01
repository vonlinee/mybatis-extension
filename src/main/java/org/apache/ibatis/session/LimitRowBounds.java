package org.apache.ibatis.session;

class LimitRowBounds implements RowBounds {

  int offset;
  int limit;

  public LimitRowBounds(int offset, int limit) {
    this.offset = offset;
    this.limit = limit;
  }

  @Override
  public int getOffset() {
    return offset;
  }

  @Override
  public int getLimit() {
    return limit;
  }
}
