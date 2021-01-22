package org.gusdb.fgputil.db.wrapper;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

public abstract class AbstractResultSetWrapper implements ResultSet {

  protected final ResultSet _underlyingResultSet;

  protected AbstractResultSetWrapper(ResultSet underlyingResultSet) {
    _underlyingResultSet = underlyingResultSet;
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return _underlyingResultSet.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return _underlyingResultSet.isWrapperFor(iface);
  }

  @Override
  public boolean next() throws SQLException {
    return _underlyingResultSet.next();
  }

  @Override
  public void close() throws SQLException {
    _underlyingResultSet.close();
  }

  @Override
  public boolean wasNull() throws SQLException {
    return _underlyingResultSet.wasNull();
  }

  @Override
  public String getString(int columnIndex) throws SQLException {
    return _underlyingResultSet.getString(columnIndex);
  }

  @Override
  public boolean getBoolean(int columnIndex) throws SQLException {
    return _underlyingResultSet.getBoolean(columnIndex);
  }

  @Override
  public byte getByte(int columnIndex) throws SQLException {
    return _underlyingResultSet.getByte(columnIndex);
  }

  @Override
  public short getShort(int columnIndex) throws SQLException {
    return _underlyingResultSet.getShort(columnIndex);
  }

  @Override
  public int getInt(int columnIndex) throws SQLException {
    return _underlyingResultSet.getInt(columnIndex);
  }

  @Override
  public long getLong(int columnIndex) throws SQLException {
    return _underlyingResultSet.getLong(columnIndex);
  }

  @Override
  public float getFloat(int columnIndex) throws SQLException {
    return _underlyingResultSet.getFloat(columnIndex);
  }

  @Override
  public double getDouble(int columnIndex) throws SQLException {
    return _underlyingResultSet.getDouble(columnIndex);
  }

  @Override
  @Deprecated
  public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
    return _underlyingResultSet.getBigDecimal(columnIndex, scale);
  }

  @Override
  public byte[] getBytes(int columnIndex) throws SQLException {
    return _underlyingResultSet.getBytes(columnIndex);
  }

  @Override
  public Date getDate(int columnIndex) throws SQLException {
    return _underlyingResultSet.getDate(columnIndex);
  }

  @Override
  public Time getTime(int columnIndex) throws SQLException {
    return _underlyingResultSet.getTime(columnIndex);
  }

  @Override
  public Timestamp getTimestamp(int columnIndex) throws SQLException {
    return _underlyingResultSet.getTimestamp(columnIndex);
  }

  @Override
  public InputStream getAsciiStream(int columnIndex) throws SQLException {
    return _underlyingResultSet.getAsciiStream(columnIndex);
  }

  @Override
  @Deprecated
  public InputStream getUnicodeStream(int columnIndex) throws SQLException {
    return _underlyingResultSet.getUnicodeStream(columnIndex);
  }

  @Override
  public InputStream getBinaryStream(int columnIndex) throws SQLException {
    return _underlyingResultSet.getBinaryStream(columnIndex);
  }

  @Override
  public String getString(String columnLabel) throws SQLException {
    return _underlyingResultSet.getString(columnLabel);
  }

  @Override
  public boolean getBoolean(String columnLabel) throws SQLException {
    return _underlyingResultSet.getBoolean(columnLabel);
  }

  @Override
  public byte getByte(String columnLabel) throws SQLException {
    return _underlyingResultSet.getByte(columnLabel);
  }

  @Override
  public short getShort(String columnLabel) throws SQLException {
    return _underlyingResultSet.getShort(columnLabel);
  }

  @Override
  public int getInt(String columnLabel) throws SQLException {
    return _underlyingResultSet.getInt(columnLabel);
  }

  @Override
  public long getLong(String columnLabel) throws SQLException {
    return _underlyingResultSet.getLong(columnLabel);
  }

  @Override
  public float getFloat(String columnLabel) throws SQLException {
    return _underlyingResultSet.getFloat(columnLabel);
  }

  @Override
  public double getDouble(String columnLabel) throws SQLException {
    return _underlyingResultSet.getDouble(columnLabel);
  }

  @Override
  @Deprecated
  public BigDecimal getBigDecimal(String columnLabel, int scale) throws SQLException {
    return _underlyingResultSet.getBigDecimal(columnLabel, scale);
  }

  @Override
  public byte[] getBytes(String columnLabel) throws SQLException {
    return _underlyingResultSet.getBytes(columnLabel);
  }

  @Override
  public Date getDate(String columnLabel) throws SQLException {
    return _underlyingResultSet.getDate(columnLabel);
  }

  @Override
  public Time getTime(String columnLabel) throws SQLException {
    return _underlyingResultSet.getTime(columnLabel);
  }

  @Override
  public Timestamp getTimestamp(String columnLabel) throws SQLException {
    return _underlyingResultSet.getTimestamp(columnLabel);
  }

  @Override
  public InputStream getAsciiStream(String columnLabel) throws SQLException {
    return _underlyingResultSet.getAsciiStream(columnLabel);
  }

  @Override
  @Deprecated
  public InputStream getUnicodeStream(String columnLabel) throws SQLException {
    return _underlyingResultSet.getUnicodeStream(columnLabel);
  }

  @Override
  public InputStream getBinaryStream(String columnLabel) throws SQLException {
    return _underlyingResultSet.getBinaryStream(columnLabel);
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return _underlyingResultSet.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    _underlyingResultSet.clearWarnings();
  }

  @Override
  public String getCursorName() throws SQLException {
    return _underlyingResultSet.getCursorName();
  }

  @Override
  public ResultSetMetaData getMetaData() throws SQLException {
    return _underlyingResultSet.getMetaData();
  }

  @Override
  public Object getObject(int columnIndex) throws SQLException {
    return _underlyingResultSet.getObject(columnIndex);
  }

  @Override
  public Object getObject(String columnLabel) throws SQLException {
    return _underlyingResultSet.getObject(columnLabel);
  }

  @Override
  public int findColumn(String columnLabel) throws SQLException {
    return _underlyingResultSet.findColumn(columnLabel);
  }

  @Override
  public Reader getCharacterStream(int columnIndex) throws SQLException {
    return _underlyingResultSet.getCharacterStream(columnIndex);
  }

  @Override
  public Reader getCharacterStream(String columnLabel) throws SQLException {
    return _underlyingResultSet.getCharacterStream(columnLabel);
  }

  @Override
  public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
    return _underlyingResultSet.getBigDecimal(columnIndex);
  }

  @Override
  public BigDecimal getBigDecimal(String columnLabel) throws SQLException {
    return _underlyingResultSet.getBigDecimal(columnLabel);
  }

  @Override
  public boolean isBeforeFirst() throws SQLException {
    return _underlyingResultSet.isBeforeFirst();
  }

  @Override
  public boolean isAfterLast() throws SQLException {
    return _underlyingResultSet.isAfterLast();
  }

  @Override
  public boolean isFirst() throws SQLException {
    return _underlyingResultSet.isFirst();
  }

  @Override
  public boolean isLast() throws SQLException {
    return _underlyingResultSet.isLast();
  }

  @Override
  public void beforeFirst() throws SQLException {
    _underlyingResultSet.beforeFirst();
  }

  @Override
  public void afterLast() throws SQLException {
    _underlyingResultSet.afterLast();
  }

  @Override
  public boolean first() throws SQLException {
    return _underlyingResultSet.first();
  }

  @Override
  public boolean last() throws SQLException {
    return _underlyingResultSet.last();
  }

  @Override
  public int getRow() throws SQLException {
    return _underlyingResultSet.getRow();
  }

  @Override
  public boolean absolute(int row) throws SQLException {
    return _underlyingResultSet.absolute(row);
  }

  @Override
  public boolean relative(int rows) throws SQLException {
    return _underlyingResultSet.relative(rows);
  }

  @Override
  public boolean previous() throws SQLException {
    return _underlyingResultSet.previous();
  }

  @Override
  public void setFetchDirection(int direction) throws SQLException {
    _underlyingResultSet.setFetchDirection(direction);
  }

  @Override
  public int getFetchDirection() throws SQLException {
    return _underlyingResultSet.getFetchDirection();
  }

  @Override
  public void setFetchSize(int rows) throws SQLException {
    _underlyingResultSet.setFetchSize(rows);
  }

  @Override
  public int getFetchSize() throws SQLException {
    return _underlyingResultSet.getFetchSize();
  }

  @Override
  public int getType() throws SQLException {
    return _underlyingResultSet.getType();
  }

  @Override
  public int getConcurrency() throws SQLException {
    return _underlyingResultSet.getConcurrency();
  }

  @Override
  public boolean rowUpdated() throws SQLException {
    return _underlyingResultSet.rowUpdated();
  }

  @Override
  public boolean rowInserted() throws SQLException {
    return _underlyingResultSet.rowInserted();
  }

  @Override
  public boolean rowDeleted() throws SQLException {
    return _underlyingResultSet.rowDeleted();
  }

  @Override
  public void updateNull(int columnIndex) throws SQLException {
    _underlyingResultSet.updateNull(columnIndex);
  }

  @Override
  public void updateBoolean(int columnIndex, boolean x) throws SQLException {
    _underlyingResultSet.updateBoolean(columnIndex, x);
  }

  @Override
  public void updateByte(int columnIndex, byte x) throws SQLException {
    _underlyingResultSet.updateByte(columnIndex, x);
  }

  @Override
  public void updateShort(int columnIndex, short x) throws SQLException {
    _underlyingResultSet.updateShort(columnIndex, x);
  }

  @Override
  public void updateInt(int columnIndex, int x) throws SQLException {
    _underlyingResultSet.updateInt(columnIndex, x);
  }

  @Override
  public void updateLong(int columnIndex, long x) throws SQLException {
    _underlyingResultSet.updateLong(columnIndex, x);
  }

  @Override
  public void updateFloat(int columnIndex, float x) throws SQLException {
    _underlyingResultSet.updateFloat(columnIndex, x);
  }

  @Override
  public void updateDouble(int columnIndex, double x) throws SQLException {
    _underlyingResultSet.updateDouble(columnIndex, x);
  }

  @Override
  public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
    _underlyingResultSet.updateBigDecimal(columnIndex, x);
  }

  @Override
  public void updateString(int columnIndex, String x) throws SQLException {
    _underlyingResultSet.updateString(columnIndex, x);
  }

  @Override
  public void updateBytes(int columnIndex, byte[] x) throws SQLException {
    _underlyingResultSet.updateBytes(columnIndex, x);
  }

  @Override
  public void updateDate(int columnIndex, Date x) throws SQLException {
    _underlyingResultSet.updateDate(columnIndex, x);
  }

  @Override
  public void updateTime(int columnIndex, Time x) throws SQLException {
    _underlyingResultSet.updateTime(columnIndex, x);
  }

  @Override
  public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
    _underlyingResultSet.updateTimestamp(columnIndex, x);
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
    _underlyingResultSet.updateAsciiStream(columnIndex, x, length);
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
    _underlyingResultSet.updateBinaryStream(columnIndex, x, length);
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
    _underlyingResultSet.updateCharacterStream(columnIndex, x, length);
  }

  @Override
  public void updateObject(int columnIndex, Object x, int scaleOrLength) throws SQLException {
    _underlyingResultSet.updateObject(columnIndex, x, scaleOrLength);
  }

  @Override
  public void updateObject(int columnIndex, Object x) throws SQLException {
    _underlyingResultSet.updateObject(columnIndex, x);
  }

  @Override
  public void updateNull(String columnLabel) throws SQLException {
    _underlyingResultSet.updateNull(columnLabel);
  }

  @Override
  public void updateBoolean(String columnLabel, boolean x) throws SQLException {
    _underlyingResultSet.updateBoolean(columnLabel, x);
  }

  @Override
  public void updateByte(String columnLabel, byte x) throws SQLException {
    _underlyingResultSet.updateByte(columnLabel, x);
  }

  @Override
  public void updateShort(String columnLabel, short x) throws SQLException {
    _underlyingResultSet.updateShort(columnLabel, x);
  }

  @Override
  public void updateInt(String columnLabel, int x) throws SQLException {
    _underlyingResultSet.updateInt(columnLabel, x);
  }

  @Override
  public void updateLong(String columnLabel, long x) throws SQLException {
    _underlyingResultSet.updateLong(columnLabel, x);
  }

  @Override
  public void updateFloat(String columnLabel, float x) throws SQLException {
    _underlyingResultSet.updateFloat(columnLabel, x);
  }

  @Override
  public void updateDouble(String columnLabel, double x) throws SQLException {
    _underlyingResultSet.updateDouble(columnLabel, x);
  }

  @Override
  public void updateBigDecimal(String columnLabel, BigDecimal x) throws SQLException {
    _underlyingResultSet.updateBigDecimal(columnLabel, x);
  }

  @Override
  public void updateString(String columnLabel, String x) throws SQLException {
    _underlyingResultSet.updateString(columnLabel, x);
  }

  @Override
  public void updateBytes(String columnLabel, byte[] x) throws SQLException {
    _underlyingResultSet.updateBytes(columnLabel, x);
  }

  @Override
  public void updateDate(String columnLabel, Date x) throws SQLException {
    _underlyingResultSet.updateDate(columnLabel, x);
  }

  @Override
  public void updateTime(String columnLabel, Time x) throws SQLException {
    _underlyingResultSet.updateTime(columnLabel, x);
  }

  @Override
  public void updateTimestamp(String columnLabel, Timestamp x) throws SQLException {
    _underlyingResultSet.updateTimestamp(columnLabel, x);
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, int length) throws SQLException {
    _underlyingResultSet.updateAsciiStream(columnLabel, x, length);
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, int length) throws SQLException {
    _underlyingResultSet.updateBinaryStream(columnLabel, x, length);
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader, int length) throws SQLException {
    _underlyingResultSet.updateCharacterStream(columnLabel, reader, length);
  }

  @Override
  public void updateObject(String columnLabel, Object x, int scaleOrLength) throws SQLException {
    _underlyingResultSet.updateObject(columnLabel, x, scaleOrLength);
  }

  @Override
  public void updateObject(String columnLabel, Object x) throws SQLException {
    _underlyingResultSet.updateObject(columnLabel, x);
  }

  @Override
  public void insertRow() throws SQLException {
    _underlyingResultSet.insertRow();
  }

  @Override
  public void updateRow() throws SQLException {
    _underlyingResultSet.updateRow();
  }

  @Override
  public void deleteRow() throws SQLException {
    _underlyingResultSet.deleteRow();
  }

  @Override
  public void refreshRow() throws SQLException {
    _underlyingResultSet.refreshRow();
  }

  @Override
  public void cancelRowUpdates() throws SQLException {
    _underlyingResultSet.cancelRowUpdates();
  }

  @Override
  public void moveToInsertRow() throws SQLException {
    _underlyingResultSet.moveToInsertRow();
  }

  @Override
  public void moveToCurrentRow() throws SQLException {
    _underlyingResultSet.moveToCurrentRow();
  }

  @Override
  public Statement getStatement() throws SQLException {
    return _underlyingResultSet.getStatement();
  }

  @Override
  public Object getObject(int columnIndex, Map<String, Class<?>> map) throws SQLException {
    return _underlyingResultSet.getObject(columnIndex, map);
  }

  @Override
  public Ref getRef(int columnIndex) throws SQLException {
    return _underlyingResultSet.getRef(columnIndex);
  }

  @Override
  public Blob getBlob(int columnIndex) throws SQLException {
    return _underlyingResultSet.getBlob(columnIndex);
  }

  @Override
  public Clob getClob(int columnIndex) throws SQLException {
    return _underlyingResultSet.getClob(columnIndex);
  }

  @Override
  public Array getArray(int columnIndex) throws SQLException {
    return _underlyingResultSet.getArray(columnIndex);
  }

  @Override
  public Object getObject(String columnLabel, Map<String, Class<?>> map) throws SQLException {
    return _underlyingResultSet.getObject(columnLabel, map);
  }

  @Override
  public Ref getRef(String columnLabel) throws SQLException {
    return _underlyingResultSet.getRef(columnLabel);
  }

  @Override
  public Blob getBlob(String columnLabel) throws SQLException {
    return _underlyingResultSet.getBlob(columnLabel);
  }

  @Override
  public Clob getClob(String columnLabel) throws SQLException {
    return _underlyingResultSet.getClob(columnLabel);
  }

  @Override
  public Array getArray(String columnLabel) throws SQLException {
    return _underlyingResultSet.getArray(columnLabel);
  }

  @Override
  public Date getDate(int columnIndex, Calendar cal) throws SQLException {
    return _underlyingResultSet.getDate(columnIndex, cal);
  }

  @Override
  public Date getDate(String columnLabel, Calendar cal) throws SQLException {
    return _underlyingResultSet.getDate(columnLabel, cal);
  }

  @Override
  public Time getTime(int columnIndex, Calendar cal) throws SQLException {
    return _underlyingResultSet.getTime(columnIndex, cal);
  }

  @Override
  public Time getTime(String columnLabel, Calendar cal) throws SQLException {
    return _underlyingResultSet.getTime(columnLabel, cal);
  }

  @Override
  public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
    return _underlyingResultSet.getTimestamp(columnIndex, cal);
  }

  @Override
  public Timestamp getTimestamp(String columnLabel, Calendar cal) throws SQLException {
    return _underlyingResultSet.getTimestamp(columnLabel, cal);
  }

  @Override
  public URL getURL(int columnIndex) throws SQLException {
    return _underlyingResultSet.getURL(columnIndex);
  }

  @Override
  public URL getURL(String columnLabel) throws SQLException {
    return _underlyingResultSet.getURL(columnLabel);
  }

  @Override
  public void updateRef(int columnIndex, Ref x) throws SQLException {
    _underlyingResultSet.updateRef(columnIndex, x);
  }

  @Override
  public void updateRef(String columnLabel, Ref x) throws SQLException {
    _underlyingResultSet.updateRef(columnLabel, x);
  }

  @Override
  public void updateBlob(int columnIndex, Blob x) throws SQLException {
    _underlyingResultSet.updateBlob(columnIndex, x);
  }

  @Override
  public void updateBlob(String columnLabel, Blob x) throws SQLException {
    _underlyingResultSet.updateBlob(columnLabel, x);
  }

  @Override
  public void updateClob(int columnIndex, Clob x) throws SQLException {
    _underlyingResultSet.updateClob(columnIndex, x);
  }

  @Override
  public void updateClob(String columnLabel, Clob x) throws SQLException {
    _underlyingResultSet.updateClob(columnLabel, x);
  }

  @Override
  public void updateArray(int columnIndex, Array x) throws SQLException {
    _underlyingResultSet.updateArray(columnIndex, x);
  }

  @Override
  public void updateArray(String columnLabel, Array x) throws SQLException {
    _underlyingResultSet.updateArray(columnLabel, x);
  }

  @Override
  public RowId getRowId(int columnIndex) throws SQLException {
    return _underlyingResultSet.getRowId(columnIndex);
  }

  @Override
  public RowId getRowId(String columnLabel) throws SQLException {
    return _underlyingResultSet.getRowId(columnLabel);
  }

  @Override
  public void updateRowId(int columnIndex, RowId x) throws SQLException {
    _underlyingResultSet.updateRowId(columnIndex, x);
  }

  @Override
  public void updateRowId(String columnLabel, RowId x) throws SQLException {
    _underlyingResultSet.updateRowId(columnLabel, x);
  }

  @Override
  public int getHoldability() throws SQLException {
    return _underlyingResultSet.getHoldability();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return _underlyingResultSet.isClosed();
  }

  @Override
  public void updateNString(int columnIndex, String nString) throws SQLException {
    _underlyingResultSet.updateNString(columnIndex, nString);
  }

  @Override
  public void updateNString(String columnLabel, String nString) throws SQLException {
    _underlyingResultSet.updateNString(columnLabel, nString);
  }

  @Override
  public void updateNClob(int columnIndex, NClob nClob) throws SQLException {
    _underlyingResultSet.updateNClob(columnIndex, nClob);
  }

  @Override
  public void updateNClob(String columnLabel, NClob nClob) throws SQLException {
    _underlyingResultSet.updateNClob(columnLabel, nClob);
  }

  @Override
  public NClob getNClob(int columnIndex) throws SQLException {
    return _underlyingResultSet.getNClob(columnIndex);
  }

  @Override
  public NClob getNClob(String columnLabel) throws SQLException {
    return _underlyingResultSet.getNClob(columnLabel);
  }

  @Override
  public SQLXML getSQLXML(int columnIndex) throws SQLException {
    return _underlyingResultSet.getSQLXML(columnIndex);
  }

  @Override
  public SQLXML getSQLXML(String columnLabel) throws SQLException {
    return _underlyingResultSet.getSQLXML(columnLabel);
  }

  @Override
  public void updateSQLXML(int columnIndex, SQLXML xmlObject) throws SQLException {
    _underlyingResultSet.updateSQLXML(columnIndex, xmlObject);
  }

  @Override
  public void updateSQLXML(String columnLabel, SQLXML xmlObject) throws SQLException {
    _underlyingResultSet.updateSQLXML(columnLabel, xmlObject);
  }

  @Override
  public String getNString(int columnIndex) throws SQLException {
    return _underlyingResultSet.getNString(columnIndex);
  }

  @Override
  public String getNString(String columnLabel) throws SQLException {
    return _underlyingResultSet.getNString(columnLabel);
  }

  @Override
  public Reader getNCharacterStream(int columnIndex) throws SQLException {
    return _underlyingResultSet.getNCharacterStream(columnIndex);
  }

  @Override
  public Reader getNCharacterStream(String columnLabel) throws SQLException {
    return _underlyingResultSet.getNCharacterStream(columnLabel);
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    _underlyingResultSet.updateNCharacterStream(columnIndex, x, length);
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
    _underlyingResultSet.updateNCharacterStream(columnLabel, reader, length);
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x, long length) throws SQLException {
    _underlyingResultSet.updateAsciiStream(columnIndex, x, length);
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x, long length) throws SQLException {
    _underlyingResultSet.updateBinaryStream(columnIndex, x, length);
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x, long length) throws SQLException {
    _underlyingResultSet.updateCharacterStream(columnIndex, x, length);
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x, long length) throws SQLException {
    _underlyingResultSet.updateAsciiStream(columnLabel, x, length);
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x, long length) throws SQLException {
    _underlyingResultSet.updateBinaryStream(columnLabel, x, length);
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader, long length) throws SQLException {
    _underlyingResultSet.updateCharacterStream(columnLabel, reader, length);
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream, long length) throws SQLException {
    _underlyingResultSet.updateBlob(columnIndex, inputStream, length);
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream, long length) throws SQLException {
    _underlyingResultSet.updateBlob(columnLabel, inputStream, length);
  }

  @Override
  public void updateClob(int columnIndex, Reader reader, long length) throws SQLException {
    _underlyingResultSet.updateClob(columnIndex, reader, length);
  }

  @Override
  public void updateClob(String columnLabel, Reader reader, long length) throws SQLException {
    _underlyingResultSet.updateClob(columnLabel, reader, length);
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader, long length) throws SQLException {
    _underlyingResultSet.updateNClob(columnIndex, reader, length);
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader, long length) throws SQLException {
    _underlyingResultSet.updateNClob(columnLabel, reader, length);
  }

  @Override
  public void updateNCharacterStream(int columnIndex, Reader x) throws SQLException {
    _underlyingResultSet.updateNCharacterStream(columnIndex, x);
  }

  @Override
  public void updateNCharacterStream(String columnLabel, Reader reader) throws SQLException {
    _underlyingResultSet.updateNCharacterStream(columnLabel, reader);
  }

  @Override
  public void updateAsciiStream(int columnIndex, InputStream x) throws SQLException {
    _underlyingResultSet.updateAsciiStream(columnIndex, x);
  }

  @Override
  public void updateBinaryStream(int columnIndex, InputStream x) throws SQLException {
    _underlyingResultSet.updateBinaryStream(columnIndex, x);
  }

  @Override
  public void updateCharacterStream(int columnIndex, Reader x) throws SQLException {
    _underlyingResultSet.updateCharacterStream(columnIndex, x);
  }

  @Override
  public void updateAsciiStream(String columnLabel, InputStream x) throws SQLException {
    _underlyingResultSet.updateAsciiStream(columnLabel, x);
  }

  @Override
  public void updateBinaryStream(String columnLabel, InputStream x) throws SQLException {
    _underlyingResultSet.updateBinaryStream(columnLabel, x);
  }

  @Override
  public void updateCharacterStream(String columnLabel, Reader reader) throws SQLException {
    _underlyingResultSet.updateCharacterStream(columnLabel, reader);
  }

  @Override
  public void updateBlob(int columnIndex, InputStream inputStream) throws SQLException {
    _underlyingResultSet.updateBlob(columnIndex, inputStream);
  }

  @Override
  public void updateBlob(String columnLabel, InputStream inputStream) throws SQLException {
    _underlyingResultSet.updateBlob(columnLabel, inputStream);
  }

  @Override
  public void updateClob(int columnIndex, Reader reader) throws SQLException {
    _underlyingResultSet.updateClob(columnIndex, reader);
  }

  @Override
  public void updateClob(String columnLabel, Reader reader) throws SQLException {
    _underlyingResultSet.updateClob(columnLabel, reader);
  }

  @Override
  public void updateNClob(int columnIndex, Reader reader) throws SQLException {
    _underlyingResultSet.updateNClob(columnIndex, reader);
  }

  @Override
  public void updateNClob(String columnLabel, Reader reader) throws SQLException {
    _underlyingResultSet.updateNClob(columnLabel, reader);
  }

  @Override
  public <T> T getObject(int columnIndex, Class<T> type) throws SQLException {
    return _underlyingResultSet.getObject(columnIndex, type);
  }

  @Override
  public <T> T getObject(String columnLabel, Class<T> type) throws SQLException {
    return _underlyingResultSet.getObject(columnLabel, type);
  }

}
