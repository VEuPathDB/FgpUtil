package org.gusdb.fgputil.db.slowquery;

import org.gusdb.fgputil.Timer;

/**
 * An extension of the Timer class that records timestamps of each step in the
 * sequence of operations that occur over the course of a SQL statement or query.
 * 
 * @author rdoherty
 */
public class SqlTimer extends Timer {

  public static interface SqlTimerLogger {
    public void submitTimer(SqlTimer timer);
  }

  public static enum SqlTimerEvents {
    STATEMENT_PREPARED,
    PARAMS_ASSIGNED,
    SQL_EXECUTED,
    RESULTS_HANDLED,
    COMPLETE;
  }

  private String _sql;
  private Long[] _times = new Long[SqlTimerEvents.values().length];

  public SqlTimer(String sql) {
    _sql = sql;
  }

  @Override
  public void restart() {
    super.restart();
    _times = new Long[SqlTimerEvents.values().length];
  }

  public void statementPrepared() {
    _times[SqlTimerEvents.STATEMENT_PREPARED.ordinal()] = getElapsed();
  }

  public void paramsAssigned() {
    _times[SqlTimerEvents.PARAMS_ASSIGNED.ordinal()] = getElapsed();
  }

  public void sqlExecuted() {
    _times[SqlTimerEvents.SQL_EXECUTED.ordinal()] = getElapsed();
  }

  public void resultsHandled() {
    _times[SqlTimerEvents.RESULTS_HANDLED.ordinal()] = getElapsed();
  }

  public void complete() {
    _times[SqlTimerEvents.COMPLETE.ordinal()] = getElapsed();
  }

  public String getSql() {
    return _sql;
  }

  public Long[] getTimes() {
    return _times;
  }
}
