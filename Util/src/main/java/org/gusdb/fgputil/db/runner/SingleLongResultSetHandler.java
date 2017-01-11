package org.gusdb.fgputil.db.runner;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;

/**
 * Handler that extracts the first value of the first row of a ResultSet and treats as Long value.
 * 
 * @author rdoherty
 */
public class SingleLongResultSetHandler implements ResultSetHandler {

  /** Status values for this handler */
  public static enum Status {
    /** if handler has not yet been used */
    NOT_YET_RUN,
    /** if no row returned by query (empty result set) */
    NO_ROW_RETURNED,
    /** if first column of first row held SQL NULL value */
    NULL_VALUE,
    /** if first column of first row held non-null value */
    NON_NULL_VALUE;
  }

  private Status _status = Status.NOT_YET_RUN;
  private Long _value = null;

  @Override
  public void handleResult(ResultSet rs) throws SQLException {

    // check for existence of a row
    if (!rs.next()) {
      _status = Status.NO_ROW_RETURNED;
      _value = null;
      return;
    }

    // try to get Long value from result set
    _status = Status.NON_NULL_VALUE;
    _value = rs.getLong(1);

    // check for null value and reset value and status if necessary
    if (rs.wasNull()) {
      _status = Status.NULL_VALUE;
      _value = null;
    }
  }

  /**
   * Returns the status of this handler
   * @return status of this handler
   */
  public Status getStatus() {
    return _status;
  }

  /**
   * Returns value retrieved from result set
   * @return value retrieved from result set
   */
  public Long getRetrievedValue() {
    return _value;
  }
}
