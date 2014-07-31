package org.gusdb.fgputil.db;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

public class WrappedConnection implements Connection {

  private final Connection _underlyingConnection;
  private final WrappedDataSource _parentDataSource;
  
  public WrappedConnection(Connection underlyingConnection, WrappedDataSource parentDataSource) {
    _underlyingConnection = underlyingConnection;
    _parentDataSource = parentDataSource;
  }

  public void close() throws SQLException {
    _parentDataSource.unregisterClosedConnection(_underlyingConnection);
    _underlyingConnection.close();
  }

  /************ ALL METHODS BELOW THIS LINE ARE SIMPLE WRAPPERS ************/
  
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return _underlyingConnection.unwrap(iface);
  }

  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return _underlyingConnection.isWrapperFor(iface);
  }

  public Statement createStatement() throws SQLException {
    return _underlyingConnection.createStatement();
  }

  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return _underlyingConnection.prepareStatement(sql);
  }

  public CallableStatement prepareCall(String sql) throws SQLException {
    return _underlyingConnection.prepareCall(sql);
  }

  public String nativeSQL(String sql) throws SQLException {
    return _underlyingConnection.nativeSQL(sql);
  }

  public void setAutoCommit(boolean autoCommit) throws SQLException {
    _underlyingConnection.setAutoCommit(autoCommit);
  }

  public boolean getAutoCommit() throws SQLException {
    return _underlyingConnection.getAutoCommit();
  }

  public void commit() throws SQLException {
    _underlyingConnection.commit();
  }

  public void rollback() throws SQLException {
    _underlyingConnection.rollback();
  }

  public boolean isClosed() throws SQLException {
    return _underlyingConnection.isClosed();
  }

  public DatabaseMetaData getMetaData() throws SQLException {
    return _underlyingConnection.getMetaData();
  }

  public void setReadOnly(boolean readOnly) throws SQLException {
    _underlyingConnection.setReadOnly(readOnly);
  }

  public boolean isReadOnly() throws SQLException {
    return _underlyingConnection.isReadOnly();
  }

  public void setCatalog(String catalog) throws SQLException {
    _underlyingConnection.setCatalog(catalog);
  }

  public String getCatalog() throws SQLException {
    return _underlyingConnection.getCatalog();
  }

  public void setTransactionIsolation(int level) throws SQLException {
    _underlyingConnection.setTransactionIsolation(level);
  }

  public int getTransactionIsolation() throws SQLException {
    return _underlyingConnection.getTransactionIsolation();
  }

  public SQLWarning getWarnings() throws SQLException {
    return _underlyingConnection.getWarnings();
  }

  public void clearWarnings() throws SQLException {
    _underlyingConnection.clearWarnings();
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    return _underlyingConnection.createStatement(resultSetType, resultSetConcurrency);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return _underlyingConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return _underlyingConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return _underlyingConnection.getTypeMap();
  }

  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    _underlyingConnection.setTypeMap(map);
  }

  public void setHoldability(int holdability) throws SQLException {
    _underlyingConnection.setHoldability(holdability);
  }

  public int getHoldability() throws SQLException {
    return _underlyingConnection.getHoldability();
  }

  public Savepoint setSavepoint() throws SQLException {
    return _underlyingConnection.setSavepoint();
  }

  public Savepoint setSavepoint(String name) throws SQLException {
    return _underlyingConnection.setSavepoint(name);
  }

  public void rollback(Savepoint savepoint) throws SQLException {
    _underlyingConnection.rollback(savepoint);
  }

  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    _underlyingConnection.releaseSavepoint(savepoint);
  }

  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    return _underlyingConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    return _underlyingConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    return _underlyingConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return _underlyingConnection.prepareStatement(sql, autoGeneratedKeys);
  }

  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return _underlyingConnection.prepareStatement(sql, columnIndexes);
  }

  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return _underlyingConnection.prepareStatement(sql, columnNames);
  }

  public Clob createClob() throws SQLException {
    return _underlyingConnection.createClob();
  }

  public Blob createBlob() throws SQLException {
    return _underlyingConnection.createBlob();
  }

  public NClob createNClob() throws SQLException {
    return _underlyingConnection.createNClob();
  }

  public SQLXML createSQLXML() throws SQLException {
    return _underlyingConnection.createSQLXML();
  }

  public boolean isValid(int timeout) throws SQLException {
    return _underlyingConnection.isValid(timeout);
  }

  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    _underlyingConnection.setClientInfo(name, value);
  }

  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    _underlyingConnection.setClientInfo(properties);
  }

  public String getClientInfo(String name) throws SQLException {
    return _underlyingConnection.getClientInfo(name);
  }

  public Properties getClientInfo() throws SQLException {
    return _underlyingConnection.getClientInfo();
  }

  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return _underlyingConnection.createArrayOf(typeName, elements);
  }

  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return _underlyingConnection.createStruct(typeName, attributes);
  }

  public void setSchema(String schema) throws SQLException {
    _underlyingConnection.setSchema(schema);
  }

  public String getSchema() throws SQLException {
    return _underlyingConnection.getSchema();
  }

  public void abort(Executor executor) throws SQLException {
    _underlyingConnection.abort(executor);
  }

  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    _underlyingConnection.setNetworkTimeout(executor, milliseconds);
  }

  public int getNetworkTimeout() throws SQLException {
    return _underlyingConnection.getNetworkTimeout();
  }

}
