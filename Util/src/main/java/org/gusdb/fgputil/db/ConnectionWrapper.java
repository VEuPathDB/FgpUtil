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

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.DbDriverInitializer;

public class ConnectionWrapper implements Connection {

  private static final Logger LOG = Logger.getLogger(ConnectionWrapper.class);

  private final Connection _underlyingConnection;
  private final DataSourceWrapper _parentDataSource;
  private final DBPlatform _underlyingPlatform;
  private final ConnectionPoolConfig _dbConfig;

  public ConnectionWrapper(Connection underlyingConnection, DataSourceWrapper parentDataSource, DBPlatform underlyingPlatform) {
    _underlyingConnection = underlyingConnection;
    _parentDataSource = parentDataSource;
    _underlyingPlatform = underlyingPlatform;
    _dbConfig = parentDataSource.getDbConfig();
  }

  public Connection getUnderlyingConnection() {
    return _underlyingConnection;
  }

  @Override
  public void close() throws SQLException {
    _parentDataSource.unregisterClosedConnection(_underlyingConnection);

    // check to see if uncommitted changes are present in this connection
    boolean uncommittedChangesPresent = checkForUncommittedChanges();

    // roll back any changes before returning connection to pool
    if (uncommittedChangesPresent) {
      SqlUtils.attemptRollback(_underlyingConnection);
    }

    // committing will cause op completion on the DB side (e.g. of in-use DB links)
    _underlyingConnection.commit();

    // reset connection-specific values back to default in case client code changed them
    _underlyingConnection.setAutoCommit(_dbConfig.getDefaultAutoCommit());
    _underlyingConnection.setReadOnly(_dbConfig.getDefaultReadOnly());

    // close the underlying connection using possibly custom logic
    ConnectionPoolConfig dbConfig = _parentDataSource.getDbConfig();
    DbDriverInitializer dbManager = DbDriverInitializer.getInstance(dbConfig.getDriverInitClass());
    dbManager.closeConnection(_underlyingConnection, dbConfig);

    if (uncommittedChangesPresent) {
      throw new UncommittedChangesException("Connection returned to pool with active transaction and uncommitted changes.");
    }
  }

  /*
   *  Please see Redmine #18073 for why we do this check and why it is handled the way it is
   */
  private boolean checkForUncommittedChanges() {
    boolean uncommittedChangesPresent = false;
    try {
      if (!_underlyingConnection.getAutoCommit() &&
          _underlyingPlatform.containsUncommittedActions(_underlyingConnection)) {
        uncommittedChangesPresent = true;
      }
    }
    catch (UnsupportedOperationException e) {
      // ignore; platform does not support this check
    }
    catch (Exception e) {
      // this feature is not meant to interrupt execution flow unless we can be sure there is a problem
      LOG.warn("Error occurred while trying to determine if uncommitted statements exist on connection", e);
    }
    return uncommittedChangesPresent;
  }

  /************ ALL METHODS BELOW THIS LINE ARE SIMPLE WRAPPERS ************/

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    return _underlyingConnection.unwrap(iface);
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return _underlyingConnection.isWrapperFor(iface);
  }

  @Override
  public Statement createStatement() throws SQLException {
    return _underlyingConnection.createStatement();
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    return _underlyingConnection.prepareStatement(sql);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    return _underlyingConnection.prepareCall(sql);
  }

  @Override
  public String nativeSQL(String sql) throws SQLException {
    return _underlyingConnection.nativeSQL(sql);
  }

  @Override
  public void setAutoCommit(boolean autoCommit) throws SQLException {
    _underlyingConnection.setAutoCommit(autoCommit);
  }

  @Override
  public boolean getAutoCommit() throws SQLException {
    return _underlyingConnection.getAutoCommit();
  }

  @Override
  public void commit() throws SQLException {
    _underlyingConnection.commit();
  }

  @Override
  public void rollback() throws SQLException {
    _underlyingConnection.rollback();
  }

  @Override
  public boolean isClosed() throws SQLException {
    return _underlyingConnection.isClosed();
  }

  @Override
  public DatabaseMetaData getMetaData() throws SQLException {
    return _underlyingConnection.getMetaData();
  }

  @Override
  public void setReadOnly(boolean readOnly) throws SQLException {
    _underlyingConnection.setReadOnly(readOnly);
  }

  @Override
  public boolean isReadOnly() throws SQLException {
    return _underlyingConnection.isReadOnly();
  }

  @Override
  public void setCatalog(String catalog) throws SQLException {
    _underlyingConnection.setCatalog(catalog);
  }

  @Override
  public String getCatalog() throws SQLException {
    return _underlyingConnection.getCatalog();
  }

  @Override
  public void setTransactionIsolation(int level) throws SQLException {
    _underlyingConnection.setTransactionIsolation(level);
  }

  @Override
  public int getTransactionIsolation() throws SQLException {
    return _underlyingConnection.getTransactionIsolation();
  }

  @Override
  public SQLWarning getWarnings() throws SQLException {
    return _underlyingConnection.getWarnings();
  }

  @Override
  public void clearWarnings() throws SQLException {
    _underlyingConnection.clearWarnings();
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    return _underlyingConnection.createStatement(resultSetType, resultSetConcurrency);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return _underlyingConnection.prepareStatement(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
      throws SQLException {
    return _underlyingConnection.prepareCall(sql, resultSetType, resultSetConcurrency);
  }

  @Override
  public Map<String, Class<?>> getTypeMap() throws SQLException {
    return _underlyingConnection.getTypeMap();
  }

  @Override
  public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
    _underlyingConnection.setTypeMap(map);
  }

  @Override
  public void setHoldability(int holdability) throws SQLException {
    _underlyingConnection.setHoldability(holdability);
  }

  @Override
  public int getHoldability() throws SQLException {
    return _underlyingConnection.getHoldability();
  }

  @Override
  public Savepoint setSavepoint() throws SQLException {
    return _underlyingConnection.setSavepoint();
  }

  @Override
  public Savepoint setSavepoint(String name) throws SQLException {
    return _underlyingConnection.setSavepoint(name);
  }

  @Override
  public void rollback(Savepoint savepoint) throws SQLException {
    _underlyingConnection.rollback(savepoint);
  }

  @Override
  public void releaseSavepoint(Savepoint savepoint) throws SQLException {
    _underlyingConnection.releaseSavepoint(savepoint);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability)
      throws SQLException {
    return _underlyingConnection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    return _underlyingConnection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
      int resultSetHoldability) throws SQLException {
    return _underlyingConnection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    return _underlyingConnection.prepareStatement(sql, autoGeneratedKeys);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    return _underlyingConnection.prepareStatement(sql, columnIndexes);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    return _underlyingConnection.prepareStatement(sql, columnNames);
  }

  @Override
  public Clob createClob() throws SQLException {
    return _underlyingConnection.createClob();
  }

  @Override
  public Blob createBlob() throws SQLException {
    return _underlyingConnection.createBlob();
  }

  @Override
  public NClob createNClob() throws SQLException {
    return _underlyingConnection.createNClob();
  }

  @Override
  public SQLXML createSQLXML() throws SQLException {
    return _underlyingConnection.createSQLXML();
  }

  @Override
  public boolean isValid(int timeout) throws SQLException {
    return _underlyingConnection.isValid(timeout);
  }

  @Override
  public void setClientInfo(String name, String value) throws SQLClientInfoException {
    _underlyingConnection.setClientInfo(name, value);
  }

  @Override
  public void setClientInfo(Properties properties) throws SQLClientInfoException {
    _underlyingConnection.setClientInfo(properties);
  }

  @Override
  public String getClientInfo(String name) throws SQLException {
    return _underlyingConnection.getClientInfo(name);
  }

  @Override
  public Properties getClientInfo() throws SQLException {
    return _underlyingConnection.getClientInfo();
  }

  @Override
  public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
    return _underlyingConnection.createArrayOf(typeName, elements);
  }

  @Override
  public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
    return _underlyingConnection.createStruct(typeName, attributes);
  }

  @Override
  public void setSchema(String schema) throws SQLException {
    _underlyingConnection.setSchema(schema);
  }

  @Override
  public String getSchema() throws SQLException {
    return _underlyingConnection.getSchema();
  }

  @Override
  public void abort(Executor executor) throws SQLException {
    _underlyingConnection.abort(executor);
  }

  @Override
  public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
    _underlyingConnection.setNetworkTimeout(executor, milliseconds);
  }

  @Override
  public int getNetworkTimeout() throws SQLException {
    return _underlyingConnection.getNetworkTimeout();
  }

}
