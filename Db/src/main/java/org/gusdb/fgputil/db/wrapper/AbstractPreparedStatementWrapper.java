package org.gusdb.fgputil.db.wrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;

public class AbstractPreparedStatementWrapper implements PreparedStatement {

  protected final PreparedStatement _underlyingPreparedStatement;

  protected AbstractPreparedStatementWrapper(PreparedStatement underlyingPreparedStatement) {
    _underlyingPreparedStatement = underlyingPreparedStatement;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return _underlyingPreparedStatement.unwrap(iface);
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    return _underlyingPreparedStatement.executeQuery(sql);
  }

  @Override
  public ResultSet executeQuery() throws SQLException {
    return _underlyingPreparedStatement.executeQuery();
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return _underlyingPreparedStatement.isWrapperFor(iface);
  }

  @Override
  public int executeUpdate(String sql) throws SQLException {
    return _underlyingPreparedStatement.executeUpdate(sql);
  }

  @Override
  public int executeUpdate() throws SQLException {
    return _underlyingPreparedStatement.executeUpdate();
  }

  @Override
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    _underlyingPreparedStatement.setNull(parameterIndex, sqlType);
  }

  @Override
  public void close() throws SQLException {
    _underlyingPreparedStatement.close();
  }

  @Override
  public int getMaxFieldSize() throws SQLException {
    return _underlyingPreparedStatement.getMaxFieldSize();
  }

  @Override
  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    _underlyingPreparedStatement.setBoolean(parameterIndex, x);
  }

  @Override
  public void setByte(int parameterIndex, byte x) throws SQLException {
    _underlyingPreparedStatement.setByte(parameterIndex, x);
  }

  @Override
  public void setMaxFieldSize(int max) throws SQLException {
    _underlyingPreparedStatement.setMaxFieldSize(max);
  }

  @Override
  public void setShort(int parameterIndex, short x) throws SQLException {
    _underlyingPreparedStatement.setShort(parameterIndex, x);
  }

  @Override
  public void setInt(int parameterIndex, int x) throws SQLException {
    _underlyingPreparedStatement.setInt(parameterIndex, x);
  }

  @Override
  public int getMaxRows() throws SQLException {
    return _underlyingPreparedStatement.getMaxRows();
  }

  @Override
  public void setLong(int parameterIndex, long x) throws SQLException {
    _underlyingPreparedStatement.setLong(parameterIndex, x);
  }

  @Override
  public void setMaxRows(int max) throws SQLException {
    _underlyingPreparedStatement.setMaxRows(max);
  }

  @Override
  public void setFloat(int parameterIndex, float x) throws SQLException {
    _underlyingPreparedStatement.setFloat(parameterIndex, x);
  }

  @Override
  public void setEscapeProcessing(boolean enable) throws SQLException {
    _underlyingPreparedStatement.setEscapeProcessing(enable);
  }

  @Override
  public void setDouble(int parameterIndex, double x) throws SQLException {
    _underlyingPreparedStatement.setDouble(parameterIndex, x);
  }

  @Override
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    _underlyingPreparedStatement.setBigDecimal(parameterIndex, x);
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    return _underlyingPreparedStatement.getQueryTimeout();
  }

  @Override
  public void setQueryTimeout(int seconds) throws SQLException {
    _underlyingPreparedStatement.setQueryTimeout(seconds);
  }

  @Override
  public void setString(int parameterIndex, String x) throws SQLException {
    _underlyingPreparedStatement.setString(parameterIndex, x);
  }

  @Override
  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    _underlyingPreparedStatement.setBytes(parameterIndex, x);
  }

  @Override
  public void cancel() throws SQLException {
    _underlyingPreparedStatement.cancel();
  }

  @Override
  public void setDate(int parameterIndex, Date x) throws SQLException {
    _underlyingPreparedStatement.setDate(parameterIndex, x);
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return _underlyingPreparedStatement.getWarnings();
  }

  @Override
  public void setTime(int parameterIndex, Time x) throws SQLException {
    _underlyingPreparedStatement.setTime(parameterIndex, x);
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    _underlyingPreparedStatement.setTimestamp(parameterIndex, x);
  }

  @Override
  public void clearWarnings() throws SQLException {
    _underlyingPreparedStatement.clearWarnings();
  }

  @Override
  public void setCursorName(String name) throws SQLException {
    _underlyingPreparedStatement.setCursorName(name);
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    _underlyingPreparedStatement.setAsciiStream(parameterIndex, x, length);
  }

  @Override
  @Deprecated
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    _underlyingPreparedStatement.setUnicodeStream(parameterIndex, x, length);
  }

  @Override
  public boolean execute(String sql) throws SQLException {
    return _underlyingPreparedStatement.execute(sql);
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    _underlyingPreparedStatement.setBinaryStream(parameterIndex, x, length);
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return _underlyingPreparedStatement.getResultSet();
  }

  @Override
  public int getUpdateCount() throws SQLException {
    return _underlyingPreparedStatement.getUpdateCount();
  }

  @Override
  public void clearParameters() throws SQLException {
    _underlyingPreparedStatement.clearParameters();
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    return _underlyingPreparedStatement.getMoreResults();
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    _underlyingPreparedStatement.setObject(parameterIndex, x, targetSqlType);
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    _underlyingPreparedStatement.setFetchDirection(direction);
  }

  @Override
  public void setObject(int parameterIndex, Object x) throws SQLException {
    _underlyingPreparedStatement.setObject(parameterIndex, x);
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return _underlyingPreparedStatement.getFetchDirection();
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    _underlyingPreparedStatement.setFetchSize(rows);
  }

  @Override
  public int getFetchSize() throws SQLException {
    return _underlyingPreparedStatement.getFetchSize();
  }

  @Override
  public boolean execute() throws SQLException {
    return _underlyingPreparedStatement.execute();
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    return _underlyingPreparedStatement.getResultSetConcurrency();
  }

  @Override
  public int getResultSetType() throws SQLException {
    return _underlyingPreparedStatement.getResultSetType();
  }

  @Override
  public void addBatch() throws SQLException {
    _underlyingPreparedStatement.addBatch();
  }

  @Override
  public void addBatch(String sql) throws SQLException {
    _underlyingPreparedStatement.addBatch(sql);
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    _underlyingPreparedStatement.setCharacterStream(parameterIndex, reader, length);
  }

  @Override
  public void clearBatch() throws SQLException {
    _underlyingPreparedStatement.clearBatch();
  }

  @Override
  public int[] executeBatch() throws SQLException {
    return _underlyingPreparedStatement.executeBatch();
  }

  @Override
  public void setRef(int parameterIndex, Ref x) throws SQLException {
    _underlyingPreparedStatement.setRef(parameterIndex, x);
  }

  @Override
  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    _underlyingPreparedStatement.setBlob(parameterIndex, x);
  }

  @Override
  public void setClob(int parameterIndex, Clob x) throws SQLException {
    _underlyingPreparedStatement.setClob(parameterIndex, x);
  }

  @Override
  public void setArray(int parameterIndex, Array x) throws SQLException {
    _underlyingPreparedStatement.setArray(parameterIndex, x);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return _underlyingPreparedStatement.getConnection();
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return _underlyingPreparedStatement.getMetaData();
  }

  @Override
  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    _underlyingPreparedStatement.setDate(parameterIndex, x, cal);
  }

  @Override
  public boolean getMoreResults(int current) throws SQLException {
    return _underlyingPreparedStatement.getMoreResults(current);
  }

  @Override
  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    _underlyingPreparedStatement.setTime(parameterIndex, x, cal);
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    return _underlyingPreparedStatement.getGeneratedKeys();
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    _underlyingPreparedStatement.setTimestamp(parameterIndex, x, cal);
  }

  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    return _underlyingPreparedStatement.executeUpdate(sql, autoGeneratedKeys);
  }

  @Override
  public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    _underlyingPreparedStatement.setNull(parameterIndex, sqlType, typeName);
  }

  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    return _underlyingPreparedStatement.executeUpdate(sql, columnIndexes);
  }

  @Override
  public void setURL(int parameterIndex, URL x) throws SQLException {
    _underlyingPreparedStatement.setURL(parameterIndex, x);
  }

  @Override
  public ParameterMetaData getParameterMetaData() throws SQLException {
    return _underlyingPreparedStatement.getParameterMetaData();
  }

  @Override
  public void setRowId(int parameterIndex, RowId x) throws SQLException {
    _underlyingPreparedStatement.setRowId(parameterIndex, x);
  }

  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    return _underlyingPreparedStatement.executeUpdate(sql, columnNames);
  }

  @Override
  public void setNString(int parameterIndex, String value) throws SQLException {
    _underlyingPreparedStatement.setNString(parameterIndex, value);
  }

  @Override
  public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
    _underlyingPreparedStatement.setNCharacterStream(parameterIndex, value, length);
  }

  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    return _underlyingPreparedStatement.execute(sql, autoGeneratedKeys);
  }

  @Override
  public void setNClob(int parameterIndex, NClob value) throws SQLException {
    _underlyingPreparedStatement.setNClob(parameterIndex, value);
  }

  @Override
  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    _underlyingPreparedStatement.setClob(parameterIndex, reader, length);
  }

  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    return _underlyingPreparedStatement.execute(sql, columnIndexes);
  }

  @Override
  public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
    _underlyingPreparedStatement.setBlob(parameterIndex, inputStream, length);
  }

  @Override
  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    _underlyingPreparedStatement.setNClob(parameterIndex, reader, length);
  }

  @Override
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    return _underlyingPreparedStatement.execute(sql, columnNames);
  }

  @Override
  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    _underlyingPreparedStatement.setSQLXML(parameterIndex, xmlObject);
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
      throws SQLException {
    _underlyingPreparedStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    return _underlyingPreparedStatement.getResultSetHoldability();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return _underlyingPreparedStatement.isClosed();
  }

  @Override
  public void setPoolable(boolean poolable) throws SQLException {
    _underlyingPreparedStatement.setPoolable(poolable);
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
    _underlyingPreparedStatement.setAsciiStream(parameterIndex, x, length);
  }

  @Override
  public boolean isPoolable() throws SQLException {
    return _underlyingPreparedStatement.isPoolable();
  }

  @Override
  public void closeOnCompletion() throws SQLException {
    _underlyingPreparedStatement.closeOnCompletion();
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
    _underlyingPreparedStatement.setBinaryStream(parameterIndex, x, length);
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException {
    return _underlyingPreparedStatement.isCloseOnCompletion();
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
    _underlyingPreparedStatement.setCharacterStream(parameterIndex, reader, length);
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    _underlyingPreparedStatement.setAsciiStream(parameterIndex, x);
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    _underlyingPreparedStatement.setBinaryStream(parameterIndex, x);
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    _underlyingPreparedStatement.setCharacterStream(parameterIndex, reader);
  }

  @Override
  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    _underlyingPreparedStatement.setNCharacterStream(parameterIndex, value);
  }

  @Override
  public void setClob(int parameterIndex, Reader reader) throws SQLException {
    _underlyingPreparedStatement.setClob(parameterIndex, reader);
  }

  @Override
  public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    _underlyingPreparedStatement.setBlob(parameterIndex, inputStream);
  }

  @Override
  public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    _underlyingPreparedStatement.setNClob(parameterIndex, reader);
  }

}
