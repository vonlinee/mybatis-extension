package org.apache.ibatis.session;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public interface Pagination<T> extends RowBounds {

  /**
   * current page index
   *
   * @return current page index
   */
  int getPage();

  @Override
  default int getOffset() {
    return (getPage() - 1) * getLimit();
  }

  /**
   * size of per page
   *
   * @return size of per page
   */
  @Override
  int getLimit();

  default void setPagedRecords(List<T> records) {
  }

  default List<T> getRecords() {
    return Collections.emptyList();
  }



  static <E> Pagination<E> valueOf(Integer page, Integer limit) {
    return new SimplePagination<>(page, limit);
  }

  static <E> Pagination<E> valueOf(int page, int limit) {
    return new SimplePagination<>(page, limit);
  }
}
