package org.gusdb.fgputil.db.runner;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.BatchUpdateExecutor;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.PreparedStatementExecutor;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.QueryExecutor;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.StatementExecutor;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.UpdateExecutor;
import org.gusdb.fgputil.db.slowquery.SqlTimer;
import org.gusdb.fgputil.db.slowquery.SqlTimer.SqlTimerLogger;

/**
 * Provides API to easily run SQL statements and queries against a database.
 * 
 * @author rdoherty
 */
public class SQLRunner {

  private static Logger LOG = Logger.getLogger(SQLRunner.class.getName());

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

  /**
   * Enables access to multiple sets of arguments, in the event that the caller
   * wishes to execute a batch operation.
   * 
   * @author rdoherty
   */
  public interface ArgumentBatch extends Iterable<Object[]> {
    /**
     * Tells SQLRunner how many instructions to add before executing a batch
     * 
     * @return how many instructions should be added before executing a batch
     */
    public int getBatchSize();

    /**
     * Tells SQLRunner what type of data is being submitted for each parameter.
     * Please use values from java.sql.Types.  A value of null for a given
     * param tells SQLRunner to intelligently 'guess' the type for that param.
     * A value of null returned by this method tells SQLRunner to guess for all
     * params.  Note guessing is less efficient.
     * 
     * @return SQL types that will suggest the type of data to be passed, or
     * null if SQLRunner is to guess the types.
     */
    public Integer[] getParameterTypes();
  }

  /**
   * The SQLRunner class has a number of options with regard to transactions.
   * If the DataSource-based constructor is used, caller can specify auto-commit
   * or to run all operations within a call inside a transaction.  If the
   * Connection-based constructor is used, no change will be made to the passed
   * Connection's commit mode.
   */
  private static enum TxStrategy {
    // Commits happen with each DB call (auto-commit).
    AUTO_COMMIT,
    // Commit happens only at end of SQLRunner call (operations occur in
    // transaction).  Note this is the default behavior when a DataSource-based
    // constructor is used.
    TRANSACTION,
    // Auto-commit setting from passed Connection is used (setting inherited).
    // Note this is the default behavior when a Connection-based constructor is
    // used.
    INHERIT;
  }

  private static final SqlTimerLogger NULL_LOGGER = new SqlTimerLogger() {
    @Override public void submitTimer(SqlTimer timer) { /* do nothing */ }
  };

  private static SqlTimerLogger _sqlLogger;

  private DataSource _ds;
  private Connection _conn;
  private String _sql;
  private String _sqlName;
  private TxStrategy _txStrategy;
  private boolean _responsibleForClosingStatementAndResultSet = true;
  private boolean _responsibleForClosingConnection;
  private long _lastExecutionTime = 0L;

  /**
   * Constructor with DataSource.  Each call to this SQLRunner will retrieve a
   * new connection from the DataSource and will run each call in a transaction,
   * committing at the end of the call.  SQL name will be auto-generated from SQL.
   * 
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   */
  public SQLRunner(DataSource ds, String sql) {
    this(ds, sql, true, EncryptionUtil.encrypt(sql));
  }

  /**
   * Constructor with DataSource.  Each call to this SQLRunner will retrieve a
   * new connection from the DataSource and will run each call in a transaction,
   * committing at the end of the call.
   * 
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   * @param sqlName name of SQL query/statement for logging
   */
  public SQLRunner(DataSource ds, String sql, String sqlName) {
    this(ds, sql, true, sqlName);
  }

  /**
   * Constructor with DataSource.  Each call to this SQLRunner will retrieve a
   * new connection from the DataSource, running in a transaction if specified.
   * SQL name will be auto-generated from SQL.
   * 
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   * @param runInTransaction if true, will wrap all batch calls in a transaction;
   * else will use auto-commit
   * @throws IllegalArgumentException if called with NO_COMMITS or INHERIT TX strategy
   */
  public SQLRunner(DataSource ds, String sql, boolean runInTransaction) {
    this(ds, sql, runInTransaction, EncryptionUtil.encrypt(sql));
  }

  /**
   * Constructor with DataSource.  Each call to this SQLRunner will retrieve a
   * new connection from the DataSource, running in a transaction if specified.
   * 
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   * @param runInTransaction if true, will wrap all batch calls in a transaction;
   * else will use auto-commit
   * @param sqlName name of SQL query/statement for logging
   * @throws IllegalArgumentException if called with NO_COMMITS or INHERIT TX strategy
   */
  public SQLRunner(DataSource ds, String sql, boolean runInTransaction, String sqlName) {
    _ds = requireNonNull(ds);
    _sql = requireNonNull(sql);
    _txStrategy = (runInTransaction ? TxStrategy.TRANSACTION : TxStrategy.AUTO_COMMIT);
    _responsibleForClosingConnection = true;
    _sqlName = sqlName;
  }

  /**
   * Constructor with Connection.  Note that callers of this constructor are
   * responsible for closing the connection they pass in.  To delegate that
   * responsibility to this class, use the constructor that takes a DataSource
   * parameter.  Will use the auto-commit setting of the passed Connection.
   * SQL name will be auto-generated from SQL.
   * 
   * @param conn connection on which to operate
   * @param sql SQL to execute via a PreparedStatement
   */
  public SQLRunner(Connection conn, String sql) {
    this(conn, sql, EncryptionUtil.encrypt(sql));
  }

  /**
   * Constructor with Connection.  Note that callers of this constructor are
   * responsible for closing the connection they pass in.  To delegate that
   * responsibility to this class, use the constructor that takes a DataSource
   * parameter.  Will use the auto-commit setting of the passed Connection.
   * 
   * @param conn connection on which to operate
   * @param sql SQL to execute via a PreparedStatement
   * @param sqlName name of SQL query/statement for logging
   */
  public SQLRunner(Connection conn, String sql, String sqlName) {
    _conn = requireNonNull(conn);
    _sql = requireNonNull(sql);
    _txStrategy = TxStrategy.INHERIT;
    _responsibleForClosingConnection = false;
    _sqlName = sqlName;
  }

  /**
   * Executes this runner's SQL and assumes no SQL parameters
   * 
   * @throws SQLRunnerException if error occurs during processing
   */
  public void executeStatement() {
    executeStatement(new Object[]{ }, null);
  }

  /**
   * Executes this runner's SQL using the passed parameter array
   * 
   * @param args SQL parameters
   * @throws SQLRunnerException if error occurs during processing
   */
  public void executeStatement(Object[] args) {
    executeStatement(args, null);
  }

  /**
   * Executes this runner's SQL using the passed parameter array and parameter
   * types.  Use java.sql.Types to as type values.
   * 
   * @param args SQL parameters
   * @param types SQL types of parameters
   * @throws SQLRunnerException if error occurs during processing
   */
  public void executeStatement(Object[] args, Integer[] types) {
    executeSql(new StatementExecutor(args, types));
  }

  /**
   * Executes a batch statement operation using sets of SQL parameters retrieved
   * from the passed argument batch.  Uses the batch's getBatchSize() method
   * to determine how many operations to group into each batch.
   * 
   * @param batch set of SQL parameter sets containing
   * @throws SQLRunnerException if error occurs during processing
   */
  public void executeStatementBatch(ArgumentBatch batch) {
    executeSql(new BatchUpdateExecutor(batch));
  }

  /**
   * Executes this runner's SQL and assumes no SQL parameters.  When doing so,
   * captures the resulting number of updates.  This method should be called
   * for insert or update operations where the caller would like to know the
   * effects of the execution.
   * 
   * @return number of rows updated
   * @throws SQLRunnerException if error occurs during processing
   */
  public int executeUpdate() {
    return executeUpdate(new Object[]{ }, null);
  }

  /**
   * Executes this runner's SQL using the passed parameter array.  When doing so,
   * captures the resulting number of updates.  This method should be called
   * for insert or update operations where the caller would like to know the
   * effects of the execution.
   * 
   * @param args SQL parameters
   * @return number of rows updated
   * @throws SQLRunnerException if error occurs during processing
   */
  public int executeUpdate(Object[] args) {
    return executeUpdate(args, null);
  }

  /**
   * Executes this runner's SQL using the passed parameter array and types.
   * When doing so, captures the resulting number of updates.  This method
   * should be called for insert or update operations where the caller would
   * like to know the effects of the execution.  Use java.sql.Types to as type
   * values.
   * 
   * @param args SQL parameters
   * @param types SQL types of parameters
   * @return number of rows updated
   * @throws SQLRunnerException if error occurs during processing
   */
  public int executeUpdate(Object[] args, Integer[] types) {
    return executeSql(new UpdateExecutor(args, types));
  }

  /**
   * Executes a batch update operation using sets of SQL parameters retrieved
   * from the passed argument batch.  Uses the batch's getBatchSize() method
   * to determine how many operations to group into each batch.  Also captures
   * the resulting number of updates, if supported by the underlying JDBC
   * driver.  Note the constants that may be returned instead from
   * <code>PreparedStatement.executeBatch()</code>.  This method simply sums
   * up the values returned from executeBatch().
   * 
   * @param batch set of SQL parameter sets containing
   * @return number of rows updated, if supported by the underlying driver
   * @throws SQLRunnerException if error occurs during processing
   */
  public int executeUpdateBatch(ArgumentBatch batch) {
    return executeSql(new BatchUpdateExecutor(batch));
  }

  /**
   * Executes an SQL query, passing results to the given handler.  This version
   * assumes no SQL parameters in this runner's SQL.
   * 
   * @param handler handler implementation to process results
   * @return the passed handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(ResultSetHandler<T> handler) {
    return executeQuery(new Object[]{ }, null, handler);
  }

  /**
   * 
   * Executes an SQL query using the passed parameter array, passing results to
   * the given handler.  
   * 
   * @param handler handler implementation to process results
   * @param args SQL parameters
   * @return the passed handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(Object[] args, ResultSetHandler<T> handler) {
    return executeQuery(args, null, handler);
  }

  /**
   * 
   * Executes an SQL query using the passed parameter array, passing results to
   * the given handler.  
   * 
   * @param handler handler implementation to process results
   * @param args SQL parameters
   * @param types SQL types of parameters
   * @return the passed handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(Object[] args, Integer[] types, ResultSetHandler<T> handler) {
    return executeSql(new QueryExecutor<T>(handler, args, types));
  }

  private <T> T executeSql(PreparedStatementExecutor<T> exec) {
    Connection conn = null;
    PreparedStatement stmt = null;
    boolean connectionSuccessful = false;
    boolean sqlExecutionSuccessful = false;
    SqlTimer timer = new SqlTimer(_sql, _sqlName);
    try {
      conn = getConnection();
      connectionSuccessful = true;
      timer.restart();

      // prepare statement
      stmt = conn.prepareStatement(_sql);
      timer.statementPrepared();

      // assign params
      exec.setParams(stmt);
      timer.paramsAssigned();

      // run SQL
      exec.runWithTimer(stmt);
      timer.sqlExecuted();
      sqlExecutionSuccessful = true;

      // handle result of SQL
      T result = exec.handleResult();
      timer.resultsHandled();

      // complete execution
      commit(conn);
      _lastExecutionTime = exec.getLastExecutionTime();
      timer.complete();
      getSqlLogger().submitTimer(timer);
      return result;

    }
    catch (Exception e) {
      // only attempt rollback if retrieved a connection in the first place
      if (connectionSuccessful) {
        attemptRollback(conn);
      }
      // since exception was thrown, close resources this instance generated even if set not responsible
      closeQuietly(exec, stmt);
      closeConnection();
      // if SQLRunnerException is thrown, propagate it; otherwise wrap in new SQLRunnerException
      throw (e instanceof SQLRunnerException ? (SQLRunnerException)e :
        new SQLRunnerException("Unable to " + (sqlExecutionSuccessful ? "process result of" : "run") +
            " SQL <" + _sql + "> with args " + exec.getParamsToString(), e));
    }
    finally {
      if (_responsibleForClosingStatementAndResultSet) {
        closeQuietly(exec, stmt);
      }
      closeConnection();
    }
  }

  private <T> void closeQuietly(PreparedStatementExecutor<T> exec, PreparedStatement stmt) {
    exec.closeQuietly();
    SqlUtils.closeQuietly(stmt);
  }

  private void commit(Connection conn) throws SQLException {
    // only need to commit here if using internal transaction
    if (_txStrategy.equals(TxStrategy.TRANSACTION)) {
      conn.commit();
    }
  }

  // this method should always be "safe" (i.e. not throw exception)
  private void attemptRollback(Connection conn) {
    if (conn == null) {
      LOG.warn("Rollback attempted on null connection.  May have failed to retrieve connection.  " +
          "See stack trace below:\n" + FormatUtil.getCurrentStackTrace());
      return;
    }
    // only need to attempt rollback if using internal transaction
    if (_txStrategy.equals(TxStrategy.TRANSACTION)) {
      try { conn.rollback(); } catch (SQLException e2) {
        // don't rethrow as it will mask the original exception
        LOG.error("Exception thrown while attempting rollback.", e2);
      }
    }
  }

  private Connection getConnection() throws SQLException {
    if (_conn == null) {
      _conn = _ds.getConnection();
      // set auto-commit to true if caller specified auto-commit
      _conn.setAutoCommit(_txStrategy.equals(TxStrategy.AUTO_COMMIT));
    }
    return _conn;
  }

  private void closeConnection() {
    if (_responsibleForClosingConnection) {
      SqlUtils.closeQuietly(_conn);
    }
  }

  public long getLastExecutionTime() {
    return _lastExecutionTime;
  }

  public static void setSqlLogger(SqlTimerLogger sqlLogger) {
    _sqlLogger = sqlLogger;
  }

  private static SqlTimerLogger getSqlLogger() {
    SqlTimerLogger tmp = _sqlLogger;
    if (tmp == null) {
      return NULL_LOGGER;
    }
    return tmp;
  }

  public SQLRunner setNotResponsibleForClosing() {
    _responsibleForClosingConnection = false;
    _responsibleForClosingStatementAndResultSet = false;
    return this;
  }
}
