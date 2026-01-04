package org.gusdb.fgputil.db.wrapper;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.UncommittedChangesException;
import org.gusdb.fgputil.db.leakmonitor.CloseableObjectType;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor.UnclosedObjectMonitorMap;
import org.gusdb.fgputil.db.platform.DBPlatform;
import org.gusdb.fgputil.db.pool.ConnectionPoolConfig;
import org.gusdb.fgputil.db.pool.DbDriverInitializer;

public class ConnectionWrapper extends AbstractConnectionWrapper {

  private static final Logger LOG = Logger.getLogger(ConnectionWrapper.class);

  private static final boolean PERFORM_UNCOMMITTED_CHANGES_CHECK = false;

  private final ConnectionPoolConfig _dbConfig;
  private final DBPlatform _underlyingPlatform;
  private final UnclosedObjectMonitorMap _unclosedObjectMonitorMap;
  private final UnclosedObjectMonitor<Connection> _unclosedObjectMonitor;

  public ConnectionWrapper(Connection underlyingConnection, ConnectionPoolConfig dbConfig, UnclosedObjectMonitorMap unclosedObjectMonitorMap) {
    super(underlyingConnection);
    _dbConfig = dbConfig;
    _underlyingPlatform = dbConfig.getPlatformEnum().getPlatformInstance();
    _unclosedObjectMonitorMap = unclosedObjectMonitorMap;
    _unclosedObjectMonitor = unclosedObjectMonitorMap.get(CloseableObjectType.Connection);
    _unclosedObjectMonitor.registerOpenedObject(underlyingConnection);
  }

  UnclosedObjectMonitorMap getUnclosedObjectMonitorMap() {
    return _unclosedObjectMonitorMap;
  }

  @Override
  public void close() throws SQLException {
    LOG.info("Closing DB Connection");
    boolean uncommittedChangesPresent = false;
    try {
      _unclosedObjectMonitor.unregisterClosedObject(_underlyingConnection);
  
      // check to see if uncommitted changes are present in this connection
      uncommittedChangesPresent =
          PERFORM_UNCOMMITTED_CHANGES_CHECK ? checkForUncommittedChanges() : false;
  
      // roll back any changes before returning connection to pool
      if (uncommittedChangesPresent) {
        SqlUtils.attemptRollback(_underlyingConnection);
      }
  
      // committing will cause op completion on the DB side (e.g. of in-use DB links)
      if (_underlyingConnection.getAutoCommit()) {
        // must turn auto-commit off to explicitly commit per JDBC spec
        _underlyingConnection.setAutoCommit(false);
        _underlyingConnection.commit();
        _underlyingConnection.setAutoCommit(true);
      }
      else {
        _underlyingConnection.commit();
      }
  
      // reset connection-specific values back to default in case client code changed them
      _underlyingConnection.setAutoCommit(_dbConfig.getDefaultAutoCommit());
      _underlyingConnection.setReadOnly(_dbConfig.getDefaultReadOnly());
  
    }
    catch (Exception e) {
      LOG.error("Error during pre-close logic for DB connections", e);
      throw e;
    }
    finally {
      // close the underlying connection using possibly custom logic
      DbDriverInitializer dbManager = DbDriverInitializer.getInstance(_dbConfig.getDriverInitClass());
      dbManager.closeConnection(_underlyingConnection, _dbConfig);
    }
  
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

  private <T extends Statement> T applyFetchSize(T statement) throws SQLException {
    statement.setFetchSize(_dbConfig.getDefaultFetchSize());
    return statement;
  }

  /********* Statement factories that apply configured fetch size and then wrap native objects *********/

  @Override
  public Statement createStatement() throws SQLException {
    Statement statement = super.createStatement();
    return new StatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql) throws SQLException {
    PreparedStatement statement = _underlyingConnection.prepareStatement(sql);
    return new PreparedStatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public CallableStatement prepareCall(String sql) throws SQLException {
    CallableStatement statement = _underlyingConnection.prepareCall(sql);
    return new CallableStatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
    Statement statement = super.createStatement(resultSetType, resultSetConcurrency);
    return new StatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    PreparedStatement statement = super.prepareStatement(sql, resultSetType, resultSetConcurrency);
    return new PreparedStatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
    CallableStatement statement = super.prepareCall(sql, resultSetType, resultSetConcurrency);
    return new CallableStatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    Statement statement = super.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    return new StatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    PreparedStatement statement = super.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    return new PreparedStatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
    CallableStatement statement = super.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    return new CallableStatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
    PreparedStatement statement = super.prepareStatement(sql, autoGeneratedKeys);
    return new PreparedStatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
    PreparedStatement statement = super.prepareStatement(sql, columnIndexes);
    return new PreparedStatementWrapper(applyFetchSize(statement), this);
  }

  @Override
  public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
    PreparedStatement statement = super.prepareStatement(sql, columnNames);
    return new PreparedStatementWrapper(applyFetchSize(statement), this);
  }
  
}
