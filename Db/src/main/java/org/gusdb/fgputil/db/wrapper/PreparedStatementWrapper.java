package org.gusdb.fgputil.db.wrapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.gusdb.fgputil.db.leakmonitor.CloseableObjectType;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor.UnclosedObjectMonitorMap;

public class PreparedStatementWrapper extends AbstractPreparedStatementWrapper implements AnyStatementWrapper {

  private final Connection _parentConnection;
  private final UnclosedObjectMonitorMap _unclosedObjectMonitorMap;
  private final UnclosedObjectMonitor<PreparedStatement> _unclosedObjectMonitor;

  public PreparedStatementWrapper(PreparedStatement underlyingStatement, ConnectionWrapper parentConnection) {
    super(underlyingStatement);
    _parentConnection = parentConnection;
    _unclosedObjectMonitorMap = parentConnection.getUnclosedObjectMonitorMap();
    _unclosedObjectMonitor = _unclosedObjectMonitorMap.get(CloseableObjectType.PreparedStatement);
    _unclosedObjectMonitor.registerOpenedObject(underlyingStatement);
  }

  @Override
  public UnclosedObjectMonitorMap getUnclosedObjectMonitorMap() {
    return _unclosedObjectMonitorMap;
  }

  @Override
  public void close() throws SQLException {
    _unclosedObjectMonitor.unregisterClosedObject(_underlyingPreparedStatement);
    super.close();
  }

  @Override
  public Connection getConnection() {
    return _parentConnection;
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    return new ResultSetWrapper(_underlyingPreparedStatement.getGeneratedKeys(), this);
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return new ResultSetWrapper(_underlyingPreparedStatement.getResultSet(), this);
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    return new ResultSetWrapper(_underlyingPreparedStatement.executeQuery(sql), this);
  }

  @Override
  public ResultSet executeQuery() throws SQLException {
    return new ResultSetWrapper(_underlyingPreparedStatement.executeQuery(), this);
  }

}
