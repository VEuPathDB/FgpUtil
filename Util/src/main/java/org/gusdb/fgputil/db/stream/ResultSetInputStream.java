package org.gusdb.fgputil.db.stream;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.Iterator;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.iterator.IteratorUtil;

public class ResultSetInputStream extends InputStream implements Wrapper {

  public interface ResultSetToBytesConverter {

    byte[] getHeader();

    byte[] getRowDelimiter();

    byte[] getRow(ResultSet rs, ResultSetColumnInfo meta) throws SQLException;

    byte[] getFooter();
  }

  private enum CurrentValueType {
    BEGIN, HEADER, ROW_DELIMETER, ROW, FOOTER, END
  }

  private final ResultSet _rs;
  private final Statement _stmt;
  private final Connection _conn;
  private final ResultSetToBytesConverter _resultConverter;
  private final ResultSetColumnInfo _columnInfo;
  private final Iterator<byte[]> _rsIterator;

  private CurrentValueType _currentValueType = CurrentValueType.BEGIN;
  private byte[] _rowBuffer = new byte[0];
  private int _rowBufferIndex = 0;

  public static ResultSetInputStream getResultSetStream(String sql, String queryName,
      DataSource ds, ResultSetToBytesConverter converter) throws SQLException {
    boolean closeDbObjects = false;
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      long startTime = System.currentTimeMillis();
      conn = ds.getConnection();
      stmt = conn.prepareStatement(sql);
      ResultSet rs = stmt.executeQuery();
      QueryLogger.logStartResultsProcessing(sql, queryName, startTime, rs);
      return new ResultSetInputStream(rs, stmt, conn, converter);
    }
    catch (SQLException e) {
      closeDbObjects = true;
      throw e;
    }
    finally {
      if (closeDbObjects) {
        SqlUtils.closeQuietly(stmt, conn);
      }
    }
  }

  public ResultSetInputStream(ResultSet resultSet, Statement statement, Connection connection,
      ResultSetToBytesConverter resultConverter) throws SQLException {
    _rs = resultSet;
    _stmt = statement;
    _conn = connection;
    _resultConverter = resultConverter;
    _columnInfo = new ResultSetColumnInfo(resultSet);
    _rsIterator = IteratorUtil.toIterator(SqlUtils.toCursor(
        resultSet, rs -> Functions.mapException(
            () -> resultConverter.getRow(rs, _columnInfo),
            sqle -> new RuntimeException(sqle))));
  }

  @Override
  public int read() throws IOException {
    if (_rowBufferIndex >= _rowBuffer.length) {
      // buffer "empty"; load next value
      _rowBufferIndex = 0;
      switch(_currentValueType) {
        case BEGIN:
          _rowBuffer = _resultConverter.getHeader();
          _currentValueType = CurrentValueType.HEADER;
          break;
        case HEADER:
          if (_rsIterator.hasNext()) {
            _rowBuffer = _rsIterator.next();
            _currentValueType = CurrentValueType.ROW;
          }
          else {
            _rowBuffer = _resultConverter.getFooter();
            _currentValueType = CurrentValueType.FOOTER;
          }
          break;
        case ROW_DELIMETER:
          _rowBuffer = _rsIterator.next();
          _currentValueType = CurrentValueType.ROW;
          break;
        case ROW:
          if (_rsIterator.hasNext()) {
            _rowBuffer = _resultConverter.getRowDelimiter();
            _currentValueType = CurrentValueType.ROW_DELIMETER;
          }
          else {
            _rowBuffer = _resultConverter.getFooter();
            _currentValueType = CurrentValueType.FOOTER;
          }
          break;
        case FOOTER:
          _rowBuffer = new byte[0];
          _currentValueType = CurrentValueType.END;
          break;
        case END:
          return -1;
      }
    }
    return _rowBuffer[_rowBufferIndex++];
  }

  /**
   * Closes the ResultSet, Statement, and Connection associated with the streamed data.
   */
  @Override
  public void close() throws IOException {
    SqlUtils.closeQuietly(_rs, _stmt, _conn);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("This class does not wrap an instance of " + iface.getName());
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }
}
