package org.gusdb.fgputil.db.wrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.ParameterMetaData;
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
import java.util.Map;

public class AbstractCallableStatement implements CallableStatement {

  protected final CallableStatement _underlyingCallableStatement;

  protected AbstractCallableStatement(CallableStatement underlyingCallableStatement) {
    _underlyingCallableStatement = underlyingCallableStatement;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return _underlyingCallableStatement.unwrap(iface);
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    return _underlyingCallableStatement.executeQuery(sql);
  }

  @Override
  public ResultSet executeQuery() throws SQLException {
    return _underlyingCallableStatement.executeQuery();
  }

  @Override
  public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException {
    _underlyingCallableStatement.registerOutParameter(parameterIndex, sqlType);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return _underlyingCallableStatement.isWrapperFor(iface);
  }

  @Override
  public int executeUpdate(String sql) throws SQLException {
    return _underlyingCallableStatement.executeUpdate(sql);
  }

  @Override
  public int executeUpdate() throws SQLException {
    return _underlyingCallableStatement.executeUpdate();
  }

  @Override
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    _underlyingCallableStatement.setNull(parameterIndex, sqlType);
  }

  @Override
  public void close() throws SQLException {
    _underlyingCallableStatement.close();
  }

  @Override
  public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException {
    _underlyingCallableStatement.registerOutParameter(parameterIndex, sqlType, scale);
  }

  @Override
  public int getMaxFieldSize() throws SQLException {
    return _underlyingCallableStatement.getMaxFieldSize();
  }

  @Override
  public void setBoolean(int parameterIndex, boolean x) throws SQLException {
    _underlyingCallableStatement.setBoolean(parameterIndex, x);
  }

  @Override
  public void setByte(int parameterIndex, byte x) throws SQLException {
    _underlyingCallableStatement.setByte(parameterIndex, x);
  }

  @Override
  public void setMaxFieldSize(int max) throws SQLException {
    _underlyingCallableStatement.setMaxFieldSize(max);
  }

  @Override
  public boolean wasNull() throws SQLException {
    return _underlyingCallableStatement.wasNull();
  }

  @Override
  public void setShort(int parameterIndex, short x) throws SQLException {
    _underlyingCallableStatement.setShort(parameterIndex, x);
  }

  @Override
  public String getString(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getString(parameterIndex);
  }

  @Override
  public void setInt(int parameterIndex, int x) throws SQLException {
    _underlyingCallableStatement.setInt(parameterIndex, x);
  }

  @Override
  public int getMaxRows() throws SQLException {
    return _underlyingCallableStatement.getMaxRows();
  }

  @Override
  public void setLong(int parameterIndex, long x) throws SQLException {
    _underlyingCallableStatement.setLong(parameterIndex, x);
  }

  @Override
  public boolean getBoolean(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getBoolean(parameterIndex);
  }

  @Override
  public void setMaxRows(int max) throws SQLException {
    _underlyingCallableStatement.setMaxRows(max);
  }

  @Override
  public void setFloat(int parameterIndex, float x) throws SQLException {
    _underlyingCallableStatement.setFloat(parameterIndex, x);
  }

  @Override
  public void setEscapeProcessing(boolean enable) throws SQLException {
    _underlyingCallableStatement.setEscapeProcessing(enable);
  }

  @Override
  public byte getByte(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getByte(parameterIndex);
  }

  @Override
  public void setDouble(int parameterIndex, double x) throws SQLException {
    _underlyingCallableStatement.setDouble(parameterIndex, x);
  }

  @Override
  public short getShort(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getShort(parameterIndex);
  }

  @Override
  public void setBigDecimal(int parameterIndex, BigDecimal x) throws SQLException {
    _underlyingCallableStatement.setBigDecimal(parameterIndex, x);
  }

  @Override
  public int getQueryTimeout() throws SQLException {
    return _underlyingCallableStatement.getQueryTimeout();
  }

  @Override
  public int getInt(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getInt(parameterIndex);
  }

  @Override
  public void setQueryTimeout(int seconds) throws SQLException {
    _underlyingCallableStatement.setQueryTimeout(seconds);
  }

  @Override
  public void setString(int parameterIndex, String x) throws SQLException {
    _underlyingCallableStatement.setString(parameterIndex, x);
  }

  @Override
  public long getLong(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getLong(parameterIndex);
  }

  @Override
  public void setBytes(int parameterIndex, byte[] x) throws SQLException {
    _underlyingCallableStatement.setBytes(parameterIndex, x);
  }

  @Override
  public float getFloat(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getFloat(parameterIndex);
  }

  @Override
  public double getDouble(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getDouble(parameterIndex);
  }

  @Override
  public void cancel() throws SQLException {
    _underlyingCallableStatement.cancel();
  }

  @Override
  public void setDate(int parameterIndex, Date x) throws SQLException {
    _underlyingCallableStatement.setDate(parameterIndex, x);
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return _underlyingCallableStatement.getWarnings();
  }

  @Override
  @Deprecated
  public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException {
    return _underlyingCallableStatement.getBigDecimal(parameterIndex, scale);
  }

  @Override
  public void setTime(int parameterIndex, Time x) throws SQLException {
    _underlyingCallableStatement.setTime(parameterIndex, x);
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x) throws SQLException {
    _underlyingCallableStatement.setTimestamp(parameterIndex, x);
  }

  @Override
  public void clearWarnings() throws SQLException {
    _underlyingCallableStatement.clearWarnings();
  }

  @Override
  public byte[] getBytes(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getBytes(parameterIndex);
  }

  @Override
  public void setCursorName(String name) throws SQLException {
    _underlyingCallableStatement.setCursorName(name);
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, int length) throws SQLException {
    _underlyingCallableStatement.setAsciiStream(parameterIndex, x, length);
  }

  @Override
  public Date getDate(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getDate(parameterIndex);
  }

  @Override
  public Time getTime(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getTime(parameterIndex);
  }

  @Override
  @Deprecated
  public void setUnicodeStream(int parameterIndex, InputStream x, int length) throws SQLException {
    _underlyingCallableStatement.setUnicodeStream(parameterIndex, x, length);
  }

  @Override
  public boolean execute(String sql) throws SQLException {
    return _underlyingCallableStatement.execute(sql);
  }

  @Override
  public Timestamp getTimestamp(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getTimestamp(parameterIndex);
  }

  @Override
  public Object getObject(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getObject(parameterIndex);
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, int length) throws SQLException {
    _underlyingCallableStatement.setBinaryStream(parameterIndex, x, length);
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return _underlyingCallableStatement.getResultSet();
  }

  @Override
  public BigDecimal getBigDecimal(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getBigDecimal(parameterIndex);
  }

  @Override
  public int getUpdateCount() throws SQLException {
    return _underlyingCallableStatement.getUpdateCount();
  }

  @Override
  public void clearParameters() throws SQLException {
    _underlyingCallableStatement.clearParameters();
  }

  @Override
  public Object getObject(int parameterIndex, Map<String, Class<?>> map) throws SQLException {
    return _underlyingCallableStatement.getObject(parameterIndex, map);
  }

  @Override
  public boolean getMoreResults() throws SQLException {
    return _underlyingCallableStatement.getMoreResults();
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType) throws SQLException {
    _underlyingCallableStatement.setObject(parameterIndex, x, targetSqlType);
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    _underlyingCallableStatement.setFetchDirection(direction);
  }

  @Override
  public Ref getRef(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getRef(parameterIndex);
  }

  @Override
  public void setObject(int parameterIndex, Object x) throws SQLException {
    _underlyingCallableStatement.setObject(parameterIndex, x);
  }

  @Override
  public Blob getBlob(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getBlob(parameterIndex);
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return _underlyingCallableStatement.getFetchDirection();
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    _underlyingCallableStatement.setFetchSize(rows);
  }

  @Override
  public Clob getClob(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getClob(parameterIndex);
  }

  @Override
  public int getFetchSize() throws SQLException {
    return _underlyingCallableStatement.getFetchSize();
  }

  @Override
  public boolean execute() throws SQLException {
    return _underlyingCallableStatement.execute();
  }

  @Override
  public Array getArray(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getArray(parameterIndex);
  }

  @Override
  public int getResultSetConcurrency() throws SQLException {
    return _underlyingCallableStatement.getResultSetConcurrency();
  }

  @Override
  public Date getDate(int parameterIndex, Calendar cal) throws SQLException {
    return _underlyingCallableStatement.getDate(parameterIndex, cal);
  }

  @Override
  public int getResultSetType() throws SQLException {
    return _underlyingCallableStatement.getResultSetType();
  }

  @Override
  public void addBatch() throws SQLException {
    _underlyingCallableStatement.addBatch();
  }

  @Override
  public void addBatch(String sql) throws SQLException {
    _underlyingCallableStatement.addBatch(sql);
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, int length) throws SQLException {
    _underlyingCallableStatement.setCharacterStream(parameterIndex, reader, length);
  }

  @Override
  public Time getTime(int parameterIndex, Calendar cal) throws SQLException {
    return _underlyingCallableStatement.getTime(parameterIndex, cal);
  }

  @Override
  public void clearBatch() throws SQLException {
    _underlyingCallableStatement.clearBatch();
  }

  @Override
  public int[] executeBatch() throws SQLException {
    return _underlyingCallableStatement.executeBatch();
  }

  @Override
  public Timestamp getTimestamp(int parameterIndex, Calendar cal) throws SQLException {
    return _underlyingCallableStatement.getTimestamp(parameterIndex, cal);
  }

  @Override
  public void setRef(int parameterIndex, Ref x) throws SQLException {
    _underlyingCallableStatement.setRef(parameterIndex, x);
  }

  @Override
  public void setBlob(int parameterIndex, Blob x) throws SQLException {
    _underlyingCallableStatement.setBlob(parameterIndex, x);
  }

  @Override
  public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException {
    _underlyingCallableStatement.registerOutParameter(parameterIndex, sqlType, typeName);
  }

  @Override
  public void setClob(int parameterIndex, Clob x) throws SQLException {
    _underlyingCallableStatement.setClob(parameterIndex, x);
  }

  @Override
  public void setArray(int parameterIndex, Array x) throws SQLException {
    _underlyingCallableStatement.setArray(parameterIndex, x);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return _underlyingCallableStatement.getConnection();
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return _underlyingCallableStatement.getMetaData();
  }

  @Override
  public void registerOutParameter(String parameterName, int sqlType) throws SQLException {
    _underlyingCallableStatement.registerOutParameter(parameterName, sqlType);
  }

  @Override
  public void setDate(int parameterIndex, Date x, Calendar cal) throws SQLException {
    _underlyingCallableStatement.setDate(parameterIndex, x, cal);
  }

  @Override
  public boolean getMoreResults(int current) throws SQLException {
    return _underlyingCallableStatement.getMoreResults(current);
  }

  @Override
  public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException {
    _underlyingCallableStatement.registerOutParameter(parameterName, sqlType, scale);
  }

  @Override
  public void setTime(int parameterIndex, Time x, Calendar cal) throws SQLException {
    _underlyingCallableStatement.setTime(parameterIndex, x, cal);
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    return _underlyingCallableStatement.getGeneratedKeys();
  }

  @Override
  public void setTimestamp(int parameterIndex, Timestamp x, Calendar cal) throws SQLException {
    _underlyingCallableStatement.setTimestamp(parameterIndex, x, cal);
  }

  @Override
  public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException {
    _underlyingCallableStatement.registerOutParameter(parameterName, sqlType, typeName);
  }

  @Override
  public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
    return _underlyingCallableStatement.executeUpdate(sql, autoGeneratedKeys);
  }

  @Override
  public void setNull(int parameterIndex, int sqlType, String typeName) throws SQLException {
    _underlyingCallableStatement.setNull(parameterIndex, sqlType, typeName);
  }

  @Override
  public URL getURL(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getURL(parameterIndex);
  }

  @Override
  public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
    return _underlyingCallableStatement.executeUpdate(sql, columnIndexes);
  }

  @Override
  public void setURL(int parameterIndex, URL x) throws SQLException {
    _underlyingCallableStatement.setURL(parameterIndex, x);
  }

  @Override
  public void setURL(String parameterName, URL val) throws SQLException {
    _underlyingCallableStatement.setURL(parameterName, val);
  }

  @Override
  public ParameterMetaData getParameterMetaData() throws SQLException {
    return _underlyingCallableStatement.getParameterMetaData();
  }

  @Override
  public void setNull(String parameterName, int sqlType) throws SQLException {
    _underlyingCallableStatement.setNull(parameterName, sqlType);
  }

  @Override
  public void setRowId(int parameterIndex, RowId x) throws SQLException {
    _underlyingCallableStatement.setRowId(parameterIndex, x);
  }

  @Override
  public int executeUpdate(String sql, String[] columnNames) throws SQLException {
    return _underlyingCallableStatement.executeUpdate(sql, columnNames);
  }

  @Override
  public void setBoolean(String parameterName, boolean x) throws SQLException {
    _underlyingCallableStatement.setBoolean(parameterName, x);
  }

  @Override
  public void setNString(int parameterIndex, String value) throws SQLException {
    _underlyingCallableStatement.setNString(parameterIndex, value);
  }

  @Override
  public void setByte(String parameterName, byte x) throws SQLException {
    _underlyingCallableStatement.setByte(parameterName, x);
  }

  @Override
  public void setShort(String parameterName, short x) throws SQLException {
    _underlyingCallableStatement.setShort(parameterName, x);
  }

  @Override
  public void setNCharacterStream(int parameterIndex, Reader value, long length) throws SQLException {
    _underlyingCallableStatement.setNCharacterStream(parameterIndex, value, length);
  }

  @Override
  public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
    return _underlyingCallableStatement.execute(sql, autoGeneratedKeys);
  }

  @Override
  public void setInt(String parameterName, int x) throws SQLException {
    _underlyingCallableStatement.setInt(parameterName, x);
  }

  @Override
  public void setNClob(int parameterIndex, NClob value) throws SQLException {
    _underlyingCallableStatement.setNClob(parameterIndex, value);
  }

  @Override
  public void setLong(String parameterName, long x) throws SQLException {
    _underlyingCallableStatement.setLong(parameterName, x);
  }

  @Override
  public void setFloat(String parameterName, float x) throws SQLException {
    _underlyingCallableStatement.setFloat(parameterName, x);
  }

  @Override
  public void setClob(int parameterIndex, Reader reader, long length) throws SQLException {
    _underlyingCallableStatement.setClob(parameterIndex, reader, length);
  }

  @Override
  public void setDouble(String parameterName, double x) throws SQLException {
    _underlyingCallableStatement.setDouble(parameterName, x);
  }

  @Override
  public boolean execute(String sql, int[] columnIndexes) throws SQLException {
    return _underlyingCallableStatement.execute(sql, columnIndexes);
  }

  @Override
  public void setBigDecimal(String parameterName, BigDecimal x) throws SQLException {
    _underlyingCallableStatement.setBigDecimal(parameterName, x);
  }

  @Override
  public void setBlob(int parameterIndex, InputStream inputStream, long length) throws SQLException {
    _underlyingCallableStatement.setBlob(parameterIndex, inputStream, length);
  }

  @Override
  public void setString(String parameterName, String x) throws SQLException {
    _underlyingCallableStatement.setString(parameterName, x);
  }

  @Override
  public void setBytes(String parameterName, byte[] x) throws SQLException {
    _underlyingCallableStatement.setBytes(parameterName, x);
  }

  @Override
  public void setNClob(int parameterIndex, Reader reader, long length) throws SQLException {
    _underlyingCallableStatement.setNClob(parameterIndex, reader, length);
  }

  @Override
  public void setDate(String parameterName, Date x) throws SQLException {
    _underlyingCallableStatement.setDate(parameterName, x);
  }

  @Override
  public boolean execute(String sql, String[] columnNames) throws SQLException {
    return _underlyingCallableStatement.execute(sql, columnNames);
  }

  @Override
  public void setSQLXML(int parameterIndex, SQLXML xmlObject) throws SQLException {
    _underlyingCallableStatement.setSQLXML(parameterIndex, xmlObject);
  }

  @Override
  public void setTime(String parameterName, Time x) throws SQLException {
    _underlyingCallableStatement.setTime(parameterName, x);
  }

  @Override
  public void setTimestamp(String parameterName, Timestamp x) throws SQLException {
    _underlyingCallableStatement.setTimestamp(parameterName, x);
  }

  @Override
  public void setObject(int parameterIndex, Object x, int targetSqlType, int scaleOrLength)
      throws SQLException {
    _underlyingCallableStatement.setObject(parameterIndex, x, targetSqlType, scaleOrLength);
  }

  @Override
  public void setAsciiStream(String parameterName, InputStream x, int length) throws SQLException {
    _underlyingCallableStatement.setAsciiStream(parameterName, x, length);
  }

  @Override
  public int getResultSetHoldability() throws SQLException {
    return _underlyingCallableStatement.getResultSetHoldability();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return _underlyingCallableStatement.isClosed();
  }

  @Override
  public void setBinaryStream(String parameterName, InputStream x, int length) throws SQLException {
    _underlyingCallableStatement.setBinaryStream(parameterName, x, length);
  }

  @Override
  public void setPoolable(boolean poolable) throws SQLException {
    _underlyingCallableStatement.setPoolable(poolable);
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x, long length) throws SQLException {
    _underlyingCallableStatement.setAsciiStream(parameterIndex, x, length);
  }

  @Override
  public void setObject(String parameterName, Object x, int targetSqlType, int scale) throws SQLException {
    _underlyingCallableStatement.setObject(parameterName, x, targetSqlType, scale);
  }

  @Override
  public boolean isPoolable() throws SQLException {
    return _underlyingCallableStatement.isPoolable();
  }

  @Override
  public void closeOnCompletion() throws SQLException {
    _underlyingCallableStatement.closeOnCompletion();
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x, long length) throws SQLException {
    _underlyingCallableStatement.setBinaryStream(parameterIndex, x, length);
  }

  @Override
  public boolean isCloseOnCompletion() throws SQLException {
    return _underlyingCallableStatement.isCloseOnCompletion();
  }

  @Override
  public void setObject(String parameterName, Object x, int targetSqlType) throws SQLException {
    _underlyingCallableStatement.setObject(parameterName, x, targetSqlType);
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader, long length) throws SQLException {
    _underlyingCallableStatement.setCharacterStream(parameterIndex, reader, length);
  }

  @Override
  public void setObject(String parameterName, Object x) throws SQLException {
    _underlyingCallableStatement.setObject(parameterName, x);
  }

  @Override
  public void setAsciiStream(int parameterIndex, InputStream x) throws SQLException {
    _underlyingCallableStatement.setAsciiStream(parameterIndex, x);
  }

  @Override
  public void setBinaryStream(int parameterIndex, InputStream x) throws SQLException {
    _underlyingCallableStatement.setBinaryStream(parameterIndex, x);
  }

  @Override
  public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException {
    _underlyingCallableStatement.setCharacterStream(parameterName, reader, length);
  }

  @Override
  public void setCharacterStream(int parameterIndex, Reader reader) throws SQLException {
    _underlyingCallableStatement.setCharacterStream(parameterIndex, reader);
  }

  @Override
  public void setDate(String parameterName, Date x, Calendar cal) throws SQLException {
    _underlyingCallableStatement.setDate(parameterName, x, cal);
  }

  @Override
  public void setNCharacterStream(int parameterIndex, Reader value) throws SQLException {
    _underlyingCallableStatement.setNCharacterStream(parameterIndex, value);
  }

  @Override
  public void setTime(String parameterName, Time x, Calendar cal) throws SQLException {
    _underlyingCallableStatement.setTime(parameterName, x, cal);
  }

  @Override
  public void setClob(int parameterIndex, Reader reader) throws SQLException {
    _underlyingCallableStatement.setClob(parameterIndex, reader);
  }

  @Override
  public void setTimestamp(String parameterName, Timestamp x, Calendar cal) throws SQLException {
    _underlyingCallableStatement.setTimestamp(parameterName, x, cal);
  }

  @Override
  public void setNull(String parameterName, int sqlType, String typeName) throws SQLException {
    _underlyingCallableStatement.setNull(parameterName, sqlType, typeName);
  }

  @Override
  public void setBlob(int parameterIndex, InputStream inputStream) throws SQLException {
    _underlyingCallableStatement.setBlob(parameterIndex, inputStream);
  }

  @Override
  public void setNClob(int parameterIndex, Reader reader) throws SQLException {
    _underlyingCallableStatement.setNClob(parameterIndex, reader);
  }

  @Override
  public String getString(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getString(parameterName);
  }

  @Override
  public boolean getBoolean(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getBoolean(parameterName);
  }

  @Override
  public byte getByte(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getByte(parameterName);
  }

  @Override
  public short getShort(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getShort(parameterName);
  }

  @Override
  public int getInt(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getInt(parameterName);
  }

  @Override
  public long getLong(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getLong(parameterName);
  }

  @Override
  public float getFloat(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getFloat(parameterName);
  }

  @Override
  public double getDouble(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getDouble(parameterName);
  }

  @Override
  public byte[] getBytes(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getBytes(parameterName);
  }

  @Override
  public Date getDate(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getDate(parameterName);
  }

  @Override
  public Time getTime(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getTime(parameterName);
  }

  @Override
  public Timestamp getTimestamp(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getTimestamp(parameterName);
  }

  @Override
  public Object getObject(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getObject(parameterName);
  }

  @Override
  public BigDecimal getBigDecimal(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getBigDecimal(parameterName);
  }

  @Override
  public Object getObject(String parameterName, Map<String, Class<?>> map) throws SQLException {
    return _underlyingCallableStatement.getObject(parameterName, map);
  }

  @Override
  public Ref getRef(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getRef(parameterName);
  }

  @Override
  public Blob getBlob(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getBlob(parameterName);
  }

  @Override
  public Clob getClob(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getClob(parameterName);
  }

  @Override
  public Array getArray(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getArray(parameterName);
  }

  @Override
  public Date getDate(String parameterName, Calendar cal) throws SQLException {
    return _underlyingCallableStatement.getDate(parameterName, cal);
  }

  @Override
  public Time getTime(String parameterName, Calendar cal) throws SQLException {
    return _underlyingCallableStatement.getTime(parameterName, cal);
  }

  @Override
  public Timestamp getTimestamp(String parameterName, Calendar cal) throws SQLException {
    return _underlyingCallableStatement.getTimestamp(parameterName, cal);
  }

  @Override
  public URL getURL(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getURL(parameterName);
  }

  @Override
  public RowId getRowId(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getRowId(parameterIndex);
  }

  @Override
  public RowId getRowId(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getRowId(parameterName);
  }

  @Override
  public void setRowId(String parameterName, RowId x) throws SQLException {
    _underlyingCallableStatement.setRowId(parameterName, x);
  }

  @Override
  public void setNString(String parameterName, String value) throws SQLException {
    _underlyingCallableStatement.setNString(parameterName, value);
  }

  @Override
  public void setNCharacterStream(String parameterName, Reader value, long length) throws SQLException {
    _underlyingCallableStatement.setNCharacterStream(parameterName, value, length);
  }

  @Override
  public void setNClob(String parameterName, NClob value) throws SQLException {
    _underlyingCallableStatement.setNClob(parameterName, value);
  }

  @Override
  public void setClob(String parameterName, Reader reader, long length) throws SQLException {
    _underlyingCallableStatement.setClob(parameterName, reader, length);
  }

  @Override
  public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException {
    _underlyingCallableStatement.setBlob(parameterName, inputStream, length);
  }

  @Override
  public void setNClob(String parameterName, Reader reader, long length) throws SQLException {
    _underlyingCallableStatement.setNClob(parameterName, reader, length);
  }

  @Override
  public NClob getNClob(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getNClob(parameterIndex);
  }

  @Override
  public NClob getNClob(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getNClob(parameterName);
  }

  @Override
  public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException {
    _underlyingCallableStatement.setSQLXML(parameterName, xmlObject);
  }

  @Override
  public SQLXML getSQLXML(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getSQLXML(parameterIndex);
  }

  @Override
  public SQLXML getSQLXML(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getSQLXML(parameterName);
  }

  @Override
  public String getNString(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getNString(parameterIndex);
  }

  @Override
  public String getNString(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getNString(parameterName);
  }

  @Override
  public Reader getNCharacterStream(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getNCharacterStream(parameterIndex);
  }

  @Override
  public Reader getNCharacterStream(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getNCharacterStream(parameterName);
  }

  @Override
  public Reader getCharacterStream(int parameterIndex) throws SQLException {
    return _underlyingCallableStatement.getCharacterStream(parameterIndex);
  }

  @Override
  public Reader getCharacterStream(String parameterName) throws SQLException {
    return _underlyingCallableStatement.getCharacterStream(parameterName);
  }

  @Override
  public void setBlob(String parameterName, Blob x) throws SQLException {
    _underlyingCallableStatement.setBlob(parameterName, x);
  }

  @Override
  public void setClob(String parameterName, Clob x) throws SQLException {
    _underlyingCallableStatement.setClob(parameterName, x);
  }

  @Override
  public void setAsciiStream(String parameterName, InputStream x, long length) throws SQLException {
    _underlyingCallableStatement.setAsciiStream(parameterName, x, length);
  }

  @Override
  public void setBinaryStream(String parameterName, InputStream x, long length) throws SQLException {
    _underlyingCallableStatement.setBinaryStream(parameterName, x, length);
  }

  @Override
  public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException {
    _underlyingCallableStatement.setCharacterStream(parameterName, reader, length);
  }

  @Override
  public void setAsciiStream(String parameterName, InputStream x) throws SQLException {
    _underlyingCallableStatement.setAsciiStream(parameterName, x);
  }

  @Override
  public void setBinaryStream(String parameterName, InputStream x) throws SQLException {
    _underlyingCallableStatement.setBinaryStream(parameterName, x);
  }

  @Override
  public void setCharacterStream(String parameterName, Reader reader) throws SQLException {
    _underlyingCallableStatement.setCharacterStream(parameterName, reader);
  }

  @Override
  public void setNCharacterStream(String parameterName, Reader value) throws SQLException {
    _underlyingCallableStatement.setNCharacterStream(parameterName, value);
  }

  @Override
  public void setClob(String parameterName, Reader reader) throws SQLException {
    _underlyingCallableStatement.setClob(parameterName, reader);
  }

  @Override
  public void setBlob(String parameterName, InputStream inputStream) throws SQLException {
    _underlyingCallableStatement.setBlob(parameterName, inputStream);
  }

  @Override
  public void setNClob(String parameterName, Reader reader) throws SQLException {
    _underlyingCallableStatement.setNClob(parameterName, reader);
  }

  @Override
  public <T> T getObject(int parameterIndex, Class<T> type) throws SQLException {
    return _underlyingCallableStatement.getObject(parameterIndex, type);
  }

  @Override
  public <T> T getObject(String parameterName, Class<T> type) throws SQLException {
    return _underlyingCallableStatement.getObject(parameterName, type);
  }

}
