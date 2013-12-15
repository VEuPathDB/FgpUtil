package org.gusdb.fgputil.db.runner;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunner.ArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;

/**
 * Container class for a set of static PreparedStatementExecutor implementations
 * for use by the {@link SQLRunner} class.  Contains the following four non-
 * abstract implementations:
 * <ul>
 *   <li>StatementExecutor: for executing single non-update SQL statements</li>
 *   <li>UpdateExecutor: for executing single update SQL statements</li>
 *   <li>BatchUpdateExecutor: for executing batch updates</li>
 *   <li>QueryExecutor: for executing SQL queries</li>
 * </ul>
 * 
 * @author rdoherty
 */
class SQLRunnerExecutors {

  private static final Logger LOG = Logger.getLogger(SQLRunnerExecutors.class);
    
  /**
   * Abstract parent of all other SQL executors.  The methods in each
   * implementation should be executed in the following order:
   * <ol>
   *   <li>{@link #setParams(PreparedStatement}</li>
   *   <li>{@link #run(PreparedStatement)}</li>
   *   <li>{@link #handleResult()}</li>
   *   <li>{@link #closeQuietly()}</li>
   * </ol>
   * 
   * @author rdoherty
   */
  static abstract class PreparedStatementExecutor {
    
    private Object[] _args;
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
    public abstract void run(PreparedStatement stmt) throws SQLException;

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
     * @throws SQLException if error occurs while handling result
     */
    public void handleResult() throws SQLException { }

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
  }
  
  /**
   * Executor for simple SQL statements for which no results are expected.
   * 
   * @author rdoherty
   */
  static class StatementExecutor extends PreparedStatementExecutor {
    public StatementExecutor(Object[] args, Integer[] types) { super(args, types); }
    @Override public void run(PreparedStatement stmt) throws SQLException { stmt.execute(); }
  }

  /**
   * Executor for SQL insert or update statements for which we would expect
   * a certain number of rows to be affected.
   * 
   * @author rdoherty
   */
  static class UpdateExecutor extends PreparedStatementExecutor {
    private int _numUpdates;
    public UpdateExecutor(Object[] args, Integer[] types) { super(args, types); }
    @Override public void run(PreparedStatement stmt) throws SQLException { _numUpdates = stmt.executeUpdate(); }
    public int getNumUpdates() { return _numUpdates; }
  }

  /**
   * Executor for an insert or update batch, in which sets of parameters are
   * applied to the same SQL and should be run in batch mode for performance.
   * 
   * @author rdoherty
   */
  static class BatchUpdateExecutor extends PreparedStatementExecutor {

    private ArgumentBatch _argBatch;
    private int _numUpdates;
    
    public BatchUpdateExecutor(ArgumentBatch argBatch) {
      super(new Object[]{ }, null);
      _argBatch = argBatch;
      validiateArgumentBatch(_argBatch);
    }

    private void validiateArgumentBatch(ArgumentBatch argBatch) {
      // make sure number of types at least matches num args in first record
      Integer[] colTypes = _argBatch.getParameterTypes();
      Iterator<Object[]> iter = _argBatch.iterator();
      Object[] first;
      if (iter.hasNext() && colTypes != null &&
          (first = iter.next()).length != colTypes.length) {
        throw new SQLRunnerException("Number of types specified (" + colTypes.length +
            ") must match number of arguments in first record (" + first.length + ").");
      }
    }

    @Override
    public void setParams(PreparedStatement stmt) throws SQLException {
      // Override and do nothing here.  We are executing updates in batch mode,
      // so params will be set during run()
    }

    @Override
    public void runWithTimer(PreparedStatement stmt) throws SQLException {
      // this class's run() method takes care of recording cumulative execution time
      run(stmt);
    }
    
    @Override
    public void run(PreparedStatement stmt) throws SQLException {
      _numUpdates = 0;
      _lastExecutionTime = 0;
      int numBatches = 0;
      int numUnexecuted = 0;
      for (Object[] args : _argBatch) {
        SqlUtils.bindParamValues(stmt, _argBatch.getParameterTypes(), args);
        stmt.addBatch();
        numUnexecuted++;
        if (numUnexecuted == _argBatch.getBatchSize()) {
          numBatches++;
          executeBatch(stmt, numBatches, numUnexecuted);
          numUnexecuted = 0;
        }
      }
      if (numUnexecuted > 0) {
        numBatches++;
        executeBatch(stmt, numBatches, numUnexecuted);
      }
    }
    
    private void executeBatch(PreparedStatement stmt, int batchNumber, int batchSize)
        throws SQLException {
      long startTime = System.currentTimeMillis();
      int[] numUpdatesArray = stmt.executeBatch();
      long currentBatchTime = (System.currentTimeMillis() - startTime);
      _lastExecutionTime += currentBatchTime;
      LOG.info(new StringBuilder("Writing batch ").append(batchNumber)
          .append(" (").append(batchSize).append(" records) took ")
          .append(currentBatchTime).append(" ms. Cumulative batch execution time: ")
          .append(_lastExecutionTime).append(" ms").toString());
      for (int count : numUpdatesArray) {
        _numUpdates += count;
      }
    }

    public int getNumUpdates() {
      return _numUpdates;
    }

    @Override
    public String getParamsToString() {
      return "Batch of arguments.";
    }
  }

  /**
   * Executor for SQL queries.  This executor requires the implementation of a
   * ResultSetHandler to process the results of the query.
   * 
   * @author rdoherty
   */
  static class QueryExecutor extends PreparedStatementExecutor {
    
    private ResultSetHandler _handler;
    private ResultSet _results;
    
    public QueryExecutor(ResultSetHandler handler, Object[] args, Integer[] types) {
      super(args, types);
      _handler = handler;
    }
    
    @Override
    public void run(PreparedStatement stmt) throws SQLException {
      _results = stmt.executeQuery();
    }
    
    @Override
    public void handleResult() throws SQLException {
      _handler.handleResult(_results);
    }
    
    @Override
    public void closeQuietly() {
      SqlUtils.closeQuietly(_results);
    }
  }
}
