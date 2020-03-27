package org.gusdb.fgputil.db.stream;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlRuntimeException;
import org.gusdb.fgputil.db.SqlUtils;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Optional;

public class ResultSetIterator<T> implements Iterator<T>, AutoCloseable {

  public interface RowConverter<T> {
    Optional<T> convert(ResultSet rs) throws SQLException;
  }

  private final ResultSet rs;

  private final RowConverter<T> converter;

  private int count = -1;

  private T next;

  private boolean hasNext = true;

  public ResultSetIterator(ResultSet rs, RowConverter<T> converter) {
    this.rs = rs;
    this.converter = converter;
    next();
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public T next() {
    var out = next;
    count++;

    if (count % 100 == 0)
      Logger.getLogger(getClass()).warn("ResultSetIterator::next(): Processed 100 records");

    try {
      while (rs.next()) {
        var tmp = converter.convert(rs);
        if (tmp.isEmpty())
          continue;

        next = tmp.get();
        return out;
      }

      hasNext = false;
      return out;
    } catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  @Override
  public void close() {
    SqlUtils.closeResultSetOnly(rs);
  }
}
