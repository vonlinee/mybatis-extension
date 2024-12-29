package org.apache.ibatis.session;

public class Pagination implements RowBounds {

  int page;
  int limit;

  public int getPage() {
    return page;
  }

  @Override
  public int getOffset() {
    return 0;
  }

  public int getPageSize() {
    return limit;
  }

  public int getLimit() {
    return limit;
  }

  public static Pagination of(int page, int limit) {
    Pagination pagination = new Pagination();
    pagination.page = page;
    pagination.limit = limit;
    return pagination;
  }
}
