package org.apache.ibatis.scripting.xmltags;

import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.scripting.SqlBuildContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;

/**
 * used when write sql with syntax like t.column in (1, 2, 3).
 *
 * @author vonlinee
 */
public final class InSqlNode extends ForEachSqlNode {

  /**
   * the colum used in sql syntax like t.column in (1, 2, 3).
   *
   * @apiNote this should a literal string, not a variable. e.g. this is optional.
   */
  @Nullable
  private final String column;

  /**
   * the alias of item in iteration. this is optional.
   */
  @Nullable
  private final String itemExpression;

  @Override
  public String getName() {
    return "in";
  }

  public InSqlNode(SqlNode contents, String collectionExpression, @Nullable String itemExpression, @NotNull Boolean nullable, @Nullable String column) {
    super(contents, collectionExpression, nullable, null, itemExpression, "(", ")", ",");
    this.column = column;
    this.itemExpression = itemExpression;
  }

  @Override
  public boolean apply(SqlBuildContext context) {
    Iterable<?> iterable = getCollection(context);
    if (iterable == null) {
      return true;
    }
    Iterator<?> iterator = iterable.iterator();
    if (!iterator.hasNext()) {
      return true;
    }

    if (column != null) {
      context.appendSql(" ");
      context.appendSql(column);
      context.appendSql(" ");
    }
    // the first element
    final Object first = iterator.next();

    final StringBuilder sb = new StringBuilder();
    sb.append("in ");
    if (iterable instanceof List) {
      sb.append("(");
      // simple type
      if (first instanceof Number) {
        sb.append(first);
        while (iterator.hasNext()) {
          sb.append(",").append(iterator.next());
        }
      } else if (first instanceof CharSequence) {
        sb.append("'").append(first).append("'");
        while (iterator.hasNext()) {
          sb.append(",'").append(iterator.next()).append("'");
        }
      } else {
        // other type
        if (itemExpression == null) {
          throw new BuilderException("the item expression cannot be null.");
        }
        // TODO
      }
      sb.append(")");
    } else {
      super.apply(context);
    }
    context.appendSql(sb.toString());
    return true;
  }
}
