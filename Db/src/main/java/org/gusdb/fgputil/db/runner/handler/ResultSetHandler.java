package org.gusdb.fgputil.db.runner.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Represents a class that will handle the ResultSet when caller uses it with
 * a <code>SQLRunner</code>.
 *
 * @author rdoherty
 */
@FunctionalInterface
public interface ResultSetHandler<T> {
  /**
   * Handles a result set.  The implementer should not attempt to close the
   * result set, as this is handled by SQLRunner.
   *
   * @param rs result set to be handled
   * @throws SQLException if a DB error occurs while reading results
   */
  public T handleResult(ResultSet rs) throws SQLException;
}
