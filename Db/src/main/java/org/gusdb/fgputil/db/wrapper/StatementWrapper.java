package org.gusdb.fgputil.db.wrapper;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.gusdb.fgputil.db.leakmonitor.CloseableObjectType;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor.UnclosedObjectMonitorMap;

public class StatementWrapper extends AbstractStatementWrapper implements AnyStatementWrapper {

  private final Connection _parentConnection;
  private final UnclosedObjectMonitorMap _unclosedObjectMonitorMap;
  private final UnclosedObjectMonitor<Statement> _unclosedObjectMonitor;

  public StatementWrapper(Statement underlyingStatement, ConnectionWrapper parentConnection) {
    super(underlyingStatement);
    _parentConnection = parentConnection;
    _unclosedObjectMonitorMap = parentConnection.getUnclosedObjectMonitorMap();
    _unclosedObjectMonitor = _unclosedObjectMonitorMap.get(CloseableObjectType.Statement);
    _unclosedObjectMonitor.registerOpenedObject(underlyingStatement);
  }

  @Override
  public UnclosedObjectMonitorMap getUnclosedObjectMonitorMap() {
    return _unclosedObjectMonitorMap;
  }

  @Override
  public void close() throws SQLException {
    _unclosedObjectMonitor.unregisterClosedObject(_underlyingStatement);
    super.close();
  }

  @Override
  public Connection getConnection() {
    return _parentConnection;
  }

  @Override
  public ResultSet getGeneratedKeys() throws SQLException {
    return new ResultSetWrapper(_underlyingStatement.getGeneratedKeys(), this);
  }

  @Override
  public ResultSet getResultSet() throws SQLException {
    return new ResultSetWrapper(_underlyingStatement.getResultSet(), this);
  }

  @Override
  public ResultSet executeQuery(String sql) throws SQLException {
    return new ResultSetWrapper(_underlyingStatement.executeQuery(sql), this);
  }

}
