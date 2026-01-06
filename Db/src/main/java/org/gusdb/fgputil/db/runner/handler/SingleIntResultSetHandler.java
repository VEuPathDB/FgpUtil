package org.gusdb.fgputil.db.runner.handler;

import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.NoSuchElementException;

/**
 * Handles a result containing a single column with an integer value.  Similar
 * to SingleLongResultSetHandler except:
 * - throws NoSuchElementException if the SQL executed does not provide a row,
 *       or a row with no columns, or a null value in the column
 * - throws ArithmeticException if the number provided does not fit into an Integer
 *
 * @author rdoherty
 */
public class SingleIntResultSetHandler implements ResultSetHandler<Integer> {

  /**
   * Handles a result containing a single column with an integer value.  Similar
   * to SingleLongResultSetHandler except throws exceptions in common error cases.
   * 
   * @param rs result set to be read
   * @throws NoSuchElementException if the SQL executed does not provide a row,
   * or a row with no columns, or a null value in the column
   * @throws ArithmeticException if the number provided does not fit into an Integer
   */
  @Override
  public Integer handleResult(ResultSet rs) throws SQLException {
    return Math.toIntExact(new SingleLongResultSetHandler().handleResult(rs).orElseThrow());
  }

}
