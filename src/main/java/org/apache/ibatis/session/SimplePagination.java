package org.apache.ibatis.session;

import java.util.List;
import java.util.Objects;

class SimplePagination<E> implements Pagination<E> {

  int page;
  int limit;
  List<E> records;

  public SimplePagination(Integer page, Integer limit) {
    this.page = Objects.requireNonNull(page, "current page cannot be null");
    this.limit = Objects.requireNonNull(limit, "page limit cannot be null");
  }

  public SimplePagination(int page, int limit) {
    this.page = page;
    this.limit = limit;
  }

  @Override
  public int getPage() {
    return page;
  }

  @Override
  public int getLimit() {
    return limit;
  }

  @Override
  public void setPagedRecords(List<E> records) {
    this.records = records;
  }
}
