package org.gusdb.fgputil.db.runner.exec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;

/**
 * Abstract parent of a set of implementations for use by the {@link SQLRunner}
 * class.  There are four non-abstract subclasses:
 * <ul>
 *   <li>StatementExecutor: for executing single non-update SQL statements</li>
 *   <li>UpdateExecutor: for executing single update SQL statements</li>
 *   <li>BatchUpdateExecutor: for executing batch updates</li>
 *   <li>QueryExecutor: for executing SQL queries</li>
 * </ul>
 * 
 * The methods in each implementation should be executed in the following order:
 * <ol>
 *   <li>{@link #setAutocommit(PreparedStatement)}</li>
 *   <li>{@link #setParams(PreparedStatement)}</li>
 *   <li>{@link #run(PreparedStatement)}</li>
 *   <li>{@link #handleResult()}</li>
 *   <li>{@link #closeQuietly()}</li>
 * </ol>
 * 
 * @author rdoherty
 */
public abstract class PreparedStatementExecutor<T> {

  protected Object[] _args;
  private Integer[] _types;
  protected long _lastExecutionTime = 0L;

  /**
   * Constructor.
   * 
   * @param args SQL parameters to be assigned to the PreparedStatement in
   * methods to be called later
   * @param types SQL parameter types for the given args
   */
  public PreparedStatementExecutor(Object[] args, Integer[] types) {
    _args = args;
    _types = types;
    if (_types != null && args.length != types.length) {
      throw new SQLRunnerException("Number of types specified (" + types.length +
          ") must match number of arguments (" + args.length + ").");
    }
  }

  /**
   * Executes the prepared statement, retrieving whatever information it
   * needs to fulfill its role during the rest of the DB interaction.
   * 
   * @param stmt statement to execute
   * @throws SQLException if error occurs while executing statement
   */
  protected abstract void run(PreparedStatement stmt) throws SQLException;

  /**
   * Assigns any SQL parameters to their proper place in the PreparedStatement
   * 
   * @param stmt statement on which to assign params
   * @throws SQLException if error occurs while setting params
   */
  public void setParams(PreparedStatement stmt) throws SQLException {
    SqlUtils.bindParamValues(stmt, _types, _args);
  }

  /**
   * Handles the result of the executed SQL
   * 
   * @throws SQLException if SQL-specific error occurs while handling result
   * @throws SQLRunnerException if another error occurs
   */
  public T handleResult() throws SQLException, SQLRunnerException { return null; }

  /**
   * Closes any resources this executor opened
   */
  public void closeQuietly() { }

  /**
   * Returns any SQL parameters as a log-friendly string
   * 
   * @return string representation of SQL parameters
   */
  public String getParamsToString() {
    if (_args.length == 0) return "[ ]";
    StringBuilder sb = new StringBuilder("[ ").append(_args[0]);
    for (int i = 1; i < _args.length; i++) {
      sb.append(", ").append(_args[i]);
    }
    return sb.append(" ]").toString();
  }

  public long getLastExecutionTime() {
    return _lastExecutionTime;
  }

  public void runWithTimer(PreparedStatement stmt) throws SQLException {
    long startTime = System.currentTimeMillis();
    run(stmt);
    _lastExecutionTime = System.currentTimeMillis() - startTime;
  }

  /**
   * @param stmt statement where fetch size may be overridden
   * @throws SQLException if error occurs
   */
  public void overrideFetchSize(PreparedStatement stmt) throws SQLException {
    // by default this is a no-op
  }

  /**
   * Sets auto-commit value on connection before statement is executed.  By
   * default, does nothing, meaning:
   * - connections created via passed DataSource will have autocommit=true
   * - connections passed in directly will not be modified
   *
   * Query-based executors should override this and set autocommit=false since
   * some DB vendors e.g. PostgreSQL have poor interaction between fetches and
   * autocommit (e.g. fetch size ignored and entire result loaded into memory,
   * or cursor is closed during commit, leading to runtime error).
   *
   * @param conn connection being prepared
   * @throws SQLException if setting autocommit fails
   */
  public void setAutocommit(Connection conn) throws SQLException { }

  /**
   * @return true if SQLRunner should close resources once execution is complete, else false
   */
  public boolean resourcesShouldBeClosed() {
    return true;
  }
}
