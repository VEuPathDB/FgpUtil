package org.gusdb.fgputil.db.runner.exec;

import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Executor for simple SQL statements for which no results are expected.
 * 
 * @author rdoherty
 */
public class StatementExecutor extends PreparedStatementExecutor<Void> {

  public StatementExecutor(Object[] args, Integer[] types) {
    super(args, types);
  }

  @Override
  public void run(PreparedStatement stmt) throws SQLException {
    stmt.execute();
  }
}
