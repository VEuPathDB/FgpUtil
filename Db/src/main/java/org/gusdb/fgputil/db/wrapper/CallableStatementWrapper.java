package org.gusdb.fgputil.db.wrapper;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.gusdb.fgputil.db.leakmonitor.CloseableObjectType;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor.UnclosedObjectMonitorMap;

public class CallableStatementWrapper extends AbstractCallableStatement implements AnyStatementWrapper {

  private final Connection _parentConnection;
  private final UnclosedObjectMonitorMap _unclosedObjectMonitorMap;
  private final UnclosedObjectMonitor<CallableStatement> _unclosedObjectMonitor;

  public CallableStatementWrapper(CallableStatement underlyingStatement, ConnectionWrapper parentConnection) {
    super(underlyingStatement);
    _parentConnection = parentConnection;
    _unclosedObjectMonitorMap = parentConnection.getUnclosedObjectMonitorMap();
    _unclosedObjectMonitor = _unclosedObjectMonitorMap.get(CloseableObjectType.CallableStatement);
    _unclosedObjectMonitor.registerOpenedObject(underlyingStatement);
  }

  @Override
  public UnclosedObjectMonitorMap getUnclosedObjectMonitorMap() {
    return _unclosedObjectMonitorMap;
  }

  @Override
  public void close() throws SQLException {
    _unclosedObjectMonitor.unregisterClosedObject(_underlyingCallableStatement);
    super.close();
  }

  @Override
  public Connection getConnection() {
    return _parentConnection;
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    return new ResultSetWrapper(_underlyingCallableStatement.getGeneratedKeys(), this);
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return new ResultSetWrapper(_underlyingCallableStatement.getResultSet(), this);
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    return new ResultSetWrapper(_underlyingCallableStatement.executeQuery(sql), this);
  }

}
