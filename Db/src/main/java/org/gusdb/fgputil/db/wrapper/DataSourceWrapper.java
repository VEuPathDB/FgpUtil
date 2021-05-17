package org.gusdb.fgputil.db.wrapper;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.leakmonitor.CloseableObjectType;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor.UnclosedObjectMonitorMap;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;

public class DataSourceWrapper extends AbstractDataSourceWrapper {

  private final ConnectionPoolConfig _dbConfig;
  private final UnclosedObjectMonitorMap _unclosedObjectMonitorMap;

  public DataSourceWrapper(String dbName, DataSource underlyingDataSource, ConnectionPoolConfig dbConfig) {
    this(dbName, underlyingDataSource, dbConfig, false);
  }

  public DataSourceWrapper(String dbName, DataSource underlyingDataSource,
      ConnectionPoolConfig dbConfig, boolean recordAllStacktraces) {
    super(underlyingDataSource);
    _dbConfig = dbConfig;
    _unclosedObjectMonitorMap = new UnclosedObjectMonitorMap(dbName, recordAllStacktraces);
  }

  @Override
  public Connection getConnection() throws SQLException {
    return new ConnectionWrapper(super.getConnection(), _dbConfig, _unclosedObjectMonitorMap);
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return new ConnectionWrapper(super.getConnection(username, password), _dbConfig, _unclosedObjectMonitorMap);
  }

  private UnclosedObjectMonitor<Connection> getConnectionMonitor() {
    return _unclosedObjectMonitorMap.get(CloseableObjectType.Connection);
  }

  public String dumpUnclosedObjectInfo() {
    // for now, only return unclosed object info for connections (may add other types later)
    return getConnectionMonitor().getUnclosedObjectInfo();
  }

  public int getNumConnectionsOpened() {
    return getConnectionMonitor().getNumOpened();
  }

  public int getNumConnectionsClosed() {
    return getConnectionMonitor().getNumClosed();
  }

  public int getConnectionsCurrentlyOpen() {
    return getConnectionMonitor().getNumCurrentlyOpen();
  }

}
