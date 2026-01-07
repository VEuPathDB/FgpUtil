package org.gusdb.fgputil.db.runner;

import static java.util.Objects.requireNonNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.util.Optional;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.EncryptionUtil;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.exec.BatchUpdateExecutor;
import org.gusdb.fgputil.db.runner.exec.QueryExecutor;
import org.gusdb.fgputil.db.runner.exec.StatementExecutor;
import org.gusdb.fgputil.db.runner.exec.UpdateExecutor;
import org.gusdb.fgputil.db.runner.handler.ResultSetHandler;
import org.gusdb.fgputil.db.runner.exec.PreparedStatementExecutor;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.db.slowquery.SqlTimer;

/**
 * Provides API to easily run SQL statements and queries against a database.
 *
 * @author rdoherty
 */
public class SQLRunner {

  private static Logger LOG = Logger.getLogger(SQLRunner.class);

  // Set a long default static timeout to avoid connection leaks if the database hangs.
  private static Integer DEFAULT_QUERY_TIMEOUT_SECONDS = 1800;

  private DataSource _ds;
  private Connection _conn;
  private String _sql;
  private String _sqlName;
  private boolean _isInternallyManagedConnection;
  private long _lastExecutionTime = 0L;
  private Duration _timeout = null;

  /**
   * Constructor with DataSource.  Each call to this SQLRunner will retrieve a
   * new connection from the DataSource. SQL name will be auto-generated from SQL.
   * Unless set otherwise, a commit will be made on the connection and it will be
   * closed at the end of the call.
   *
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   */
  public SQLRunner(DataSource ds, String sql) {
    this(ds, sql, generateName(sql));
  }

  /**
   * Constructor with DataSource.  Each call to this SQLRunner will retrieve a
   * new connection from the DataSource.  Unless set otherwise, a commit will be
   * made on the connection and it will be closed at the end of the call.
   *
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   * @param sqlName name of SQL query/statement for logging
   */
  public SQLRunner(DataSource ds, String sql, String sqlName) {
    _ds = requireNonNull(ds);
    _sql = requireNonNull(sql);
    _sqlName = sqlName;
    _isInternallyManagedConnection = true;
  }

  /**
   * Constructor with Connection.  Callers of this constructor are responsible for
   * closing the passed connection.  To delegate that responsibility to this class,
   * use the constructor that takes a DataSource parameter.  Will use the auto-commit
   * setting of the passed Connection.  SQL name will be auto-generated from SQL.
   *
   * @param conn connection on which to operate
   * @param sql SQL to execute via a PreparedStatement
   */
  public SQLRunner(Connection conn, String sql) {
    this(conn, sql, generateName(sql));
  }

  /**
   * Constructor with Connection.  Callers of this constructor are responsible for
   * closing the passed connection.  To delegate that responsibility to this class,
   * use the constructor that takes a DataSource parameter.  Will use the auto-commit
   * setting of the passed Connection.
   *
   * @param conn connection on which to operate
   * @param sql SQL to execute via a PreparedStatement
   * @param sqlName name of SQL query/statement for logging
   */
  public SQLRunner(Connection conn, String sql, String sqlName) {
    _conn = requireNonNull(conn);
    _sql = requireNonNull(sql);
    _sqlName = sqlName;
    _isInternallyManagedConnection = false;
  }

  /**
   * Set a global default connection timeout that gets used for all SQLRunner instances if no override is set. Set to null
   * if no global default timeout is desired.
   *
   * @param timeout Default timeout value.
   */
  public static void setDefaultConnectionTimeout(Duration timeout) {
    DEFAULT_QUERY_TIMEOUT_SECONDS = Optional.ofNullable(timeout)
        .map(d -> (int) d.getSeconds())
        .orElse(null);
  }

  /**
   * Set the connection timeout for this instance of SQLRunner. This takes precedence over the default timeout set via
   * setDefaultConnectionTimeout.
   *
   * @param timeout Timeout value to propagate to JDBC statement.
   * @return this instance of SQLRunner.
   */
  public SQLRunner setConnectionTimeout(Duration timeout) {
    _timeout = timeout;
    return this;
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
   * Executes this runner's SQL using the passed parameter builder.
   *
   * @param params set of parameters as set in a builder
   * @throws SQLRunnerException if error occurs during processing
   */
  public void executeStatement(ParamBuilder params) {
    executeStatement(params.getParamValues(), params.getParamTypes());
  }

  /**
   * Executes a batch statement operation using sets of SQL parameters retrieved
   * from the passed argument batch.  Uses the batch's getBatchSize() method
   * to determine how many operations to group into each batch.
   *
   * No special transaction management logic is used, meaning that If:
   *
   * 1. This SQLRunner was created with a DataSource (i.e. autocommit on when connection fetched), OR
   * 2. This SQLRunner was created with a Connection with autocommit turned on
   * 
   * Then: commits will be made after each batch is executed, including the final batch.
   *
   * If the SQLRunner was created with a Connection with autocommit turned off,
   * no commits will be made, neither after individual batches nor after all
   * batches have been executed.  This enables external transaction management
   * for the entire dataset or in combination with other statements.
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
   * Executes this runner's SQL using the passed parameter builder.
   * When doing so, captures the resulting number of updates.  This method
   * should be called for insert or update operations where the caller would
   * like to know the effects of the execution.
   *
   * @param params set of parameters as set in a builder
   * @return number of rows updated
   * @throws SQLRunnerException if error occurs during processing
   */
  public int executeUpdate(ParamBuilder params) {
    return executeUpdate(params.getParamValues(), params.getParamTypes());
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
   * No special transaction management logic is used, meaning that if:
   *
   * 1. This SQLRunner was created with a DataSource, OR
   * 2. This SQLRunner was created with a Connection with autocommit turned on
   * 
   * Then: commits will be made after each batch is executed, including the final batch.
   *
   * If the SQLRunner was created with a Connection with autocommit turned off,
   * no commits will be made, either after individual batches or after all
   * batches have been executed.  This enables external transaction management
   * for the entire dataset or in combination with other updates.
   *
   * @param batch set of SQL parameter sets containing
   * @return number of rows updated, if supported by the underlying driver
   * @throws SQLRunnerException if error occurs during processing
   */
  public int executeUpdateBatch(ArgumentBatch batch) {
    return executeSql(new BatchUpdateExecutor(batch));
  }

  /**
   * Executes an SQL query, passing results to the given handler.  This method
   * assumes no SQL parameters in this runner's SQL.
   *
   * @param handler handler implementation to process results
   * @return value returned by the handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(ResultSetHandler<T> handler) {
    return executeQuery(new QueryFlags(), handler);
  }

  /**
   * Executes an SQL query, passing results to the given handler.  This method
   * assumes no SQL parameters in this runner's SQL.
   *
   * @param queryFlags custom query flags to use during this query
   * @param handler handler implementation to process results
   * @return value returned by the handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(QueryFlags queryFlags, ResultSetHandler<T> handler) {
    return executeQuery(queryFlags, new Object[]{ }, null, handler);
  }

  /**
   * Executes an SQL query using the passed parameter array, passing results to
   * the given handler.  
   *
   * @param args SQL parameters
   * @param types SQL types of parameters
   * @param handler handler implementation to process results
   * @return value returned by the handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(Object[] args, Integer[] types, ResultSetHandler<T> handler) {
    return executeQuery(new QueryFlags(), args, types, handler);
  }

  /**
   * Executes an SQL query using the passed parameter array and using a
   * custom fetch size, passing results to the given handler.
   *
   * @param queryFlags custom query flags to use during this query
   * @param args SQL parameters
   * @param types SQL types of parameters
   * @param handler handler implementation to process results
   * @return value returned by the handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(QueryFlags queryFlags, Object[] args, Integer[] types, ResultSetHandler<T> handler) {
    return executeSql(new QueryExecutor<T>(queryFlags, args, types, handler));
  }

  /**
   * Executes an SQL query using the passed parameters, passing
   * results to the given handler.
   *
   * @param params set of parameters as set in a builder
   * @param handler handler implementation to process results
   * @return value returned by the handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(ParamBuilder params, ResultSetHandler<T> handler) {
    return executeQuery(new QueryFlags(), params.getParamValues(), params.getParamTypes(), handler);
  }

  /**
   * Executes an SQL query using the passed parameters, passing
   * results to the given handler.
   *
   * @param queryFlags custom query flags to use during this query
   * @param params set of parameters as set in a builder
   * @param handler handler implementation to process results
   * @return value returned by the handler
   * @throws SQLRunnerException if error occurs during processing
   */
  public <T> T executeQuery(QueryFlags queryFlags, ParamBuilder params, ResultSetHandler<T> handler) {
    return executeQuery(queryFlags, params.getParamValues(), params.getParamTypes(), handler);
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
      exec.setAutocommit(conn);
      timer.restart();

      // prepare statement
      stmt = conn.prepareStatement(
          _sql,
          // enables efficiency in DB since result row can be discarded once delivered
          ResultSet.TYPE_FORWARD_ONLY,
          // enables efficiency in DB since extra state must be maintained to enable read/write results
          ResultSet.CONCUR_READ_ONLY,
          // better resource management on DB since cursors are freed at commit rather than waiting for close;
          //   actual connection close MAY happen much later depending on connection pool implementation
          //ResultSet.CLOSE_CURSORS_AT_COMMIT, <-- Unsupported by Oracle :(
          ResultSet.HOLD_CURSORS_OVER_COMMIT
      );

      // Prioritize override query timeout and fallback to global query timeout.
      if (DEFAULT_QUERY_TIMEOUT_SECONDS != null) {
        stmt.setQueryTimeout(DEFAULT_QUERY_TIMEOUT_SECONDS);
      }
      if (_timeout != null) {
        stmt.setQueryTimeout((int) _timeout.getSeconds());
      }

      exec.overrideFetchSize(stmt);
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
      _lastExecutionTime = exec.getLastExecutionTime();
      timer.complete();
      QueryLogger.submitTimer(timer);
      return result;

    }
    catch (Exception e) {
      // only attempt rollback if retrieved a connection in the first place
      if (connectionSuccessful) {
        attemptRollback(conn);
      }

      // since exception was thrown, close resources this instance generated even if set not responsible
      closeResources(exec, stmt, conn);

      // if SQLRunnerException is thrown, propagate it; otherwise wrap in new SQLRunnerException
      throw (e instanceof SQLRunnerException ? (SQLRunnerException)e :
        new SQLRunnerException("Unable to " + (sqlExecutionSuccessful ? "process result of" : "run") +
            " SQL <" + _sql + "> with args " + exec.getParamsToString(), e));
    }
    finally {
      // close resources if not configured to allow the returned object to
      //   handle resource closing.
      if (exec.resourcesShouldBeClosed()) {
        closeResources(exec, stmt, conn);
      }
    }
  }

  /**
   * Closes resources created by this SQLRunner; this always includes the
   * statement and (if present) ResultSet, and sometimes includes the connection
   * if the caller passed a DataSource.  If the caller passed in an existing
   * Connection; SQLRunner is not responsible for it.
   */
  private void closeResources(PreparedStatementExecutor<?> exec, PreparedStatement stmt, Connection conn) {
    exec.closeQuietly();
    SqlUtils.closeQuietly(stmt);
    if (_isInternallyManagedConnection) {
      SqlUtils.closeQuietly(conn);
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
    try {
      if (!conn.getAutoCommit()) {
        conn.rollback();
      }
    }
    catch (SQLException e2) {
      // don't rethrow as it will mask the original exception
      LOG.error("Exception thrown while attempting rollback.", e2);
    }
  }

  private Connection getConnection() throws SQLException {
    if (_conn == null) {
      _conn = _ds.getConnection();
      _conn.setAutoCommit(true); // driver/pool should do this but make sure
    }
    return _conn;
  }

  public long getLastExecutionTime() {
    return _lastExecutionTime;
  }

  public static String generateName(String sql) {
    return EncryptionUtil.encrypt(sql);
  }
}
