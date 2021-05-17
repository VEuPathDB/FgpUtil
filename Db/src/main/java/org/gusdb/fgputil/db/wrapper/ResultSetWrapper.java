package org.gusdb.fgputil.db.wrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.gusdb.fgputil.db.leakmonitor.CloseableObjectType;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor;
import org.gusdb.fgputil.db.leakmonitor.UnclosedObjectMonitor.UnclosedObjectMonitorMap;

public class ResultSetWrapper extends AbstractResultSetWrapper {

  private final Statement _parentStatement;
  private final UnclosedObjectMonitorMap _unclosedObjectMonitorMap;
  private final UnclosedObjectMonitor<ResultSet> _unclosedObjectMonitor;

  public ResultSetWrapper(ResultSet underlyingResultSet, AnyStatementWrapper parentStatement) {
    super(underlyingResultSet);
    _parentStatement = parentStatement;
    _unclosedObjectMonitorMap = parentStatement.getUnclosedObjectMonitorMap();
    _unclosedObjectMonitor = _unclosedObjectMonitorMap.get(CloseableObjectType.ResultSet);
    _unclosedObjectMonitor.registerOpenedObject(underlyingResultSet);
  }

  @Override
  public void close() throws SQLException {
    _unclosedObjectMonitor.unregisterClosedObject(_underlyingResultSet);
    super.close();
  }

  @Override
  public Statement getStatement() {
    return _parentStatement;
  }
}
