package org.gusdb.fgputil.db.stream;

import org.gusdb.fgputil.db.SqlRuntimeException;
import org.gusdb.fgputil.db.SqlUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class ResultSetIterator<T> implements Iterator<T>, AutoCloseable {

  public interface RowConverter<T> {
    Optional<T> convert(ResultSet rs) throws SQLException;
  }

  private final ResultSet rs;
  private final RowConverter<T> converter;

  private boolean _connectionClosed = false;

  // By default, this class is responsible for the connection that produced
  // its ResultSet.  This is part of a strategy of having classes take
  // responsibility by default.  Doings so means if a developer forgets to turn
  // OFF responsibility, they get a runtime exception the first time the code
  // is run.  This is preferable to forgetting to turn ON responsibility, which
  // results in a silent connection leak.
  private boolean _isResponsibleForConnection = true;

  private T next;

  private boolean hasNext = true;

  public ResultSetIterator(ResultSet rs, RowConverter<T> converter) {
    this.rs = rs;
    this.converter = converter;
    next(); // returns null; preloads the first item
  }

  @Override
  public boolean hasNext() {
    return hasNext;
  }

  @Override
  public T next() {
    if (!hasNext) {
      throw new NoSuchElementException("No more elements.");
    }

    var out = next;
    try {
      while (rs.next()) {
        var tmp = converter.convert(rs);
        if (tmp.isPresent()) {
          next = tmp.get();
          return out;
        }
      }

      hasNext = false;
      return out;
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  public int numRemaining() {
    int count = 0;
    while (hasNext()) {
      next();
      count++;
    }
    return count;
  }

  public ResultSetIterator<T> setResponsibleForConnection(boolean isResponsibleForConnection) {
    _isResponsibleForConnection = isResponsibleForConnection;
    return this;
  }

  @Override
  public void close() {
    try {
      Connection conn = rs.getStatement().getConnection();
      SqlUtils.closeResultSetAndStatementOnly(rs);
      if (_isResponsibleForConnection) {
        SqlUtils.closeQuietly(conn);
        _connectionClosed = true;
      }
    }
    catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  public boolean isClosed() {
    return _connectionClosed;
  }

}
