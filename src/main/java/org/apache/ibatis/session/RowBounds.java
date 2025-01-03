/*
 *    Copyright 2009-2022 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.session;

/**
 * @author Clinton Begin
 */
public interface RowBounds {

  int NO_ROW_OFFSET = 0;
  int NO_ROW_LIMIT = Integer.MAX_VALUE;

  RowBounds DEFAULT = new LimitRowBounds(NO_ROW_OFFSET, RowBounds.NO_ROW_LIMIT);

  int getOffset();

  int getLimit();

  static RowBounds valueOf(Integer offset, Integer limit) {
    return new LimitRowBounds(offset == null ? NO_ROW_OFFSET : offset, limit == null ? NO_ROW_LIMIT : limit);
  }
}
