package org.gusdb.fgputil.db.runner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.BatchUpdateExecutor;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.PreparedStatementExecutor;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.QueryExecutor;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.StatementExecutor;
import org.gusdb.fgputil.db.runner.SQLRunnerExecutors.UpdateExecutor;

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
  public interface ResultSetHandler {
    /**
     * Handles a result set.  The implementer should not attempt to close the
     * result set, as this is handled by SQLRunner.
     * 
     * @param rs result set to be handled
     * @throws SQLException if a DB error occurs while reading results
     */
    public void handleResult(ResultSet rs) throws SQLException;
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
  
  private DataSource _ds;
  private Connection _conn;
  private String _sql;
  private TxStrategy _txStrategy;
  private boolean _responsibleForConnection;
  private long _lastExecutionTime = 0L;
  
  /**
   * Constructor with DataSource.  Each call to this SQLRunner will retrieve a
   * new connection from the DataSource and will run each call in a transaction,
   * committing at the end of the call.
   * 
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   */
  public SQLRunner(DataSource ds, String sql) {
    this(ds, sql, true);
  }
  
  /**
   * Constructor with DataSource.  Each call to this SQLRunner will retrieve a
   * new connection from the DataSource, running in a transaction if specified.
   * 
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   * @param runInTransaction if true, will wrap all batch calls in a transaction;
   * else will use auto-commit
   * @throws IllegalArgumentException if called with NO_COMMITS or INHERIT TX strategy
   */
  public SQLRunner(DataSource ds, String sql, boolean runInTransaction) {
    _ds = ds;
    _sql = sql;
    _txStrategy = (runInTransaction ? TxStrategy.TRANSACTION : TxStrategy.AUTO_COMMIT);
    _responsibleForConnection = true;
  }

  /**
   * Constructor with Connection.  Note that callers of this constructor are
   * responsible for closing the connection they pass in.  To delegate that
   * responsibility to this class, use the constructor that takes a DataSource
   * parameter.  Will use the auto-commit setting of the passed Connection.
   * 
   * @param conn connection on which to operate
   * @param sql SQL to execute via a PreparedStatement
   */
  public SQLRunner(Connection conn, String sql) {
    _conn = conn;
    _sql = sql;
    _txStrategy = TxStrategy.INHERIT;
    _responsibleForConnection = false;
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
    UpdateExecutor runner = new UpdateExecutor(args, types);
    executeSql(runner);
    return runner.getNumUpdates();
  }
  
  /**
   * Executes a batch update operation using sets of SQL parameters retrieved
   * from the passed argument batch.  Uses the batch's getBatchSize() method
   * to determine how many operations to group into each batch.  Also captures
   * the resulting number of updates.
   * 
   * @param batch set of SQL parameter sets containing
   * @return number of rows updated
   * @throws SQLRunnerException if error occurs during processing
   */
  public int executeUpdateBatch(ArgumentBatch batch) {
    BatchUpdateExecutor runner = new BatchUpdateExecutor(batch);
    executeSql(runner);
    return runner.getNumUpdates();
  }

  /**
   * Executes an SQL query, passing results to the given handler.  This version
   * assumes no SQL parameters in this runner's SQL.
   * 
   * @param handler handler implementation to process results
   * @throws SQLRunnerException if error occurs during processing
   */
  public void executeQuery(ResultSetHandler handler) {
    executeQuery(new Object[]{ }, null, handler);
  }
  
  /**
   * 
   * Executes an SQL query using the passed parameter array, passing results to
   * the given handler.  
   * 
   * @param handler handler implementation to process results
   * @param args SQL parameters
   * @throws SQLRunnerException if error occurs during processing
   */
  public void executeQuery(Object[] args, ResultSetHandler handler) {
    executeQuery(args, null, handler);
  }
  
  /**
   * 
   * Executes an SQL query using the passed parameter array, passing results to
   * the given handler.  
   * 
   * @param handler handler implementation to process results
   * @param args SQL parameters
   * @param types SQL types of parameters
   * @throws SQLRunnerException if error occurs during processing
   */
  public void executeQuery(Object[] args, Integer[] types, ResultSetHandler handler) {
    executeSql(new QueryExecutor(handler, args, types));
  }
  
  private void executeSql(PreparedStatementExecutor exec) {
    Connection conn = null;
    PreparedStatement stmt = null;
    boolean connectionSuccessful = false;
    try {
      conn = getConnection();
      connectionSuccessful = true;
      // record start
      stmt = conn.prepareStatement(_sql);
      // record prepare
      exec.setParams(stmt);
      // record params set
      exec.runWithTimer(stmt);
      // record execute
      exec.handleResult();
      // record handling and write results
      commit(conn);
      _lastExecutionTime = exec.getLastExecutionTime();
    }
    catch (SQLException e) {
      // only attempt rollback if retrieved a connection in the first place
      if (connectionSuccessful) {
        attemptRollback(conn);
      }
      throw new SQLRunnerException("Unable to run query with SQL <" + _sql + "> and args: " + exec.getParamsToString(), e);
    }
    finally {
      exec.closeQuietly();
      SqlUtils.closeQuietly(stmt);
      closeConnection();
    }
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
    if (_responsibleForConnection) {
      _conn = _ds.getConnection();
      // set auto-commit to true if caller specified auto-commit
      _conn.setAutoCommit(_txStrategy.equals(TxStrategy.AUTO_COMMIT));
    }
    return _conn;
  }
  
  private void closeConnection() {
    if (_responsibleForConnection) {
      SqlUtils.closeQuietly(_conn);
    }
  }

  public long getLastExecutionTime() {
    return _lastExecutionTime;
  }
}
