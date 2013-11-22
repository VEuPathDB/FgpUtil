package org.gusdb.fgputil.db.runner;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
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

  @SuppressWarnings("unused")
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
  }
  
  private boolean _responsibleForConnection;
  private DataSource _ds;
  private Connection _conn;
  private String _sql;
  
  /**
   * Constructor with DataSource.
   * 
   * @param ds data source on which to operate
   * @param sql SQL to execute via a PreparedStatement
   */
  public SQLRunner(DataSource ds, String sql) {
    _ds = ds;
    _sql = sql;
    _responsibleForConnection = true;
  }

  /**
   * Constructor with Connection.  Note that callers of this constructor are
   * responsible for closing the connection they pass in.  To delegate that
   * responsibility to this class, use the constructor that takes a DataSource
   * parameter.
   * 
   * @param conn connection on which to operate
   * @param sql SQL to execute via a PreparedStatement
   */
  public SQLRunner(Connection conn, String sql) {
    _conn = conn;
    _sql = sql;
    _responsibleForConnection = false;
  }
  
  /**
   * Executes this runner's SQL and assumes no SQL parameters
   */
  public void executeStatement() {
    executeStatement(new Object[]{ });
  }
  
  /**
   * Executes this runner's SQL using the passed parameter array
   * 
   * @param args SQL parameters
   */
  public void executeStatement(Object[] args) {
    executeSql(new StatementExecutor(args));
  }
  
  /**
   * Executes a batch statement operation using sets of SQL parameters retrieved
   * from the passed argument batch.  Uses the batch's getBatchSize() method
   * to determine how many operations to group into each batch.
   * 
   * @param batch set of SQL parameter sets containing
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
   */
  public int executeUpdate() {
    return executeUpdate(new Object[]{ });
  }
  
  /**
   * Executes this runner's SQL using the passed parameter array.  When doing so,
   * captures the resulting number of updates.  This method should be called
   * for insert or update operations where the caller would like to know the
   * effects of the execution.
   * 
   * @param args SQL parameters
   * @return number of rows updated
   */
  public int executeUpdate(Object[] args) {
    UpdateExecutor runner = new UpdateExecutor(args);
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
   */
  public void executeQuery(ResultSetHandler handler) {
    executeQuery(new Object[]{ }, handler);
  }
  
  /**
   * 
   * Executes an SQL query using the passed parameter array, passing results to
   * the given handler.  
   * 
   * @param args SQL parameters
   * @param handler handler implementation to process results
   */
  public void executeQuery(Object[] args, ResultSetHandler handler) {
    executeSql(new QueryExecutor(args, handler));
  }
  
  private void executeSql(PreparedStatementExecutor exec) {
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      conn = getConnection();
      // record start
      stmt = conn.prepareStatement(_sql);
      // record prepare
      exec.setParams(stmt);
      // record params set
      exec.run(stmt);
      // record execute
      exec.handleResult();
      // record handling and write results
    }
    catch (SQLException e) {
      // record error and write results
      throw new SQLRunnerException("Unable to run query with SQL <" + _sql + "> and args: " + exec.getParamsToString(), e);
    }
    finally {
      exec.closeQuietly();
      SqlUtils.closeQuietly(stmt);
      closeConnection();
    }
  }

  private Connection getConnection() throws SQLException {
    if (_responsibleForConnection) {
      _conn = _ds.getConnection();
    }
    return _conn;
  }
  
  private void closeConnection() {
    if (_responsibleForConnection) {
      SqlUtils.closeQuietly(_conn);
    }
  }
}
