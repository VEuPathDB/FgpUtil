package org.gusdb.fgputil.db.stream;

import org.gusdb.fgputil.db.SqlRuntimeException;
import org.gusdb.fgputil.db.SqlUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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

  private boolean firstRowLoaded = false;
  private boolean hasNext = true;

  private T next;


  public ResultSetIterator(ResultSet rs, RowConverter<T> converter) {
    this.rs = rs;
    this.converter = converter;
  }

  @Override
  public boolean hasNext() {
    preloadFirstRow();
    return hasNext;
  }

  @Override
  public T next() {
    preloadFirstRow();
    if (!hasNext) {
      throw new NoSuchElementException("No more elements.");
    }
    return loadNext();
  }

  // called by hasNext() and next() to avoid exceptions in constructor that could
  //   lead to a connection leak if this is instantiated in a try-with-resources
  private void preloadFirstRow() {
    if (!firstRowLoaded) {
      loadNext(); // will always return null here
      firstRowLoaded = true;
    }
  }

  private T loadNext() {
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
      Statement statement = rs.getStatement();
      Connection conn = statement.getConnection();
      SqlUtils.closeQuietly(rs, statement);
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
