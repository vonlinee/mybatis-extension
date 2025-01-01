package org.apache.ibatis.session;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface Pagination<T> extends RowBounds {

  int getPage();

  @Override
  default int getOffset() {
    return (getPage() - 1) * getLimit();
  }

  @Override
  int getLimit();

  default void setPagedRecords(List<T> records) {
  }

  default List<T> getRecords() {
    return Collections.emptyList();
  }

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

  static <E> Pagination<E> of(Integer page, Integer limit) {
    return new SimplePagination<>(page, limit);
  }

  static <E> Pagination<E> of(int page, int limit) {
    return new SimplePagination<>(page, limit);
  }
}
