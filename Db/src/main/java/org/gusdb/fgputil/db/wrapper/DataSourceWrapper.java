package org.gusdb.fgputil.db.wrapper;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.NoSuchElementException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.leakmonitor.CloseableObjectType;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor.UnclosedObjectMonitorMap;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.functional.FunctionalInterfaces.SupplierWithException;

public class DataSourceWrapper extends AbstractDataSourceWrapper {

  private static final Logger LOG = Logger.getLogger(DataSourceWrapper.class);

  private static final String IDLE_OBJECT_MESSAGE = "Timeout waiting for idle object";

  private final String _dbName;
  private final ConnectionPoolConfig _dbConfig;
  private final UnclosedObjectMonitorMap _unclosedObjectMonitorMap;
  private final boolean _dumpStackTracesOnPoolExhaustion;

  public DataSourceWrapper(String dbName, DataSource underlyingDataSource, ConnectionPoolConfig dbConfig) {
    this(dbName, underlyingDataSource, dbConfig, false, true);
  }

  public DataSourceWrapper(String dbName, DataSource underlyingDataSource,
      ConnectionPoolConfig dbConfig, boolean recordAllStacktraces, boolean dumpStackTracesOnPoolExhaustion) {
    super(underlyingDataSource);
    _dbName = dbName;
    _dbConfig = dbConfig;
    _unclosedObjectMonitorMap = new UnclosedObjectMonitorMap(dbName, recordAllStacktraces);
    _dumpStackTracesOnPoolExhaustion = dumpStackTracesOnPoolExhaustion;
  }

  @Override
  public Connection getConnection() throws SQLException {
    return checkExhaustedPool(() ->
      new ConnectionWrapper(super.getConnection(), _dbConfig, _unclosedObjectMonitorMap));
  }

  @Override
  public Connection getConnection(String username, String password) throws SQLException {
    return checkExhaustedPool(() ->
      new ConnectionWrapper(super.getConnection(username, password), _dbConfig, _unclosedObjectMonitorMap));
  }

  private Connection checkExhaustedPool(SupplierWithException<Connection> connectionSupplier) throws SQLException {
    try {
      return connectionSupplier.get();
    }
    catch (SQLException e) {
      if (_dumpStackTracesOnPoolExhaustion) {
        // check if this exception is likely caused by pool exhaustion
        Throwable cause = e.getCause();
        if (cause != null &&
            cause instanceof NoSuchElementException &&
            IDLE_OBJECT_MESSAGE.equals(cause.getMessage())) {
          // looks like connection pool is exhausted, causing a request failure
          LOG.warn("\n\nUnable to retrieve a database connection from the pool for " + _dbName +
              " before timeout.  This application may be under heavy load or there may be a " +
              "connection leak.  Will dump open connection information, followed by the " +
              "exception which triggered this message.\n\n" + dumpUnclosedObjectInfo() + "\n\n", e);
        }
      }
      throw e;
    }
    catch (RuntimeException e) {
      throw e;
    }
    catch (Exception e) {
      // this should never happen
      throw new RuntimeException(e);
    }
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
