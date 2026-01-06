package org.gusdb.fgputil.db.runner.exec;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.gusdb.fgputil.db.runner.SQLRunnerException;

/**
 * Executor for SQL insert or update statements for which we would expect
 * a certain number of rows to be affected.
 * 
 * @author rdoherty
 */
public class UpdateExecutor extends PreparedStatementExecutor<Integer> {

  private int _numUpdates;

  public UpdateExecutor(Object[] args, Integer[] types) {
    super(args, types);
  }

  @Override
  public void run(PreparedStatement stmt) throws SQLException {
    _numUpdates = stmt.executeUpdate();
  }

  @Override
  public Integer handleResult() throws SQLException, SQLRunnerException {
    return _numUpdates;
  }
}
