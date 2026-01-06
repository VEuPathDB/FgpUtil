package org.gusdb.fgputil.db.runner.exec;

import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.ArgumentBatch;
import org.gusdb.fgputil.db.runner.SQLRunnerException;

/**
 * Executor for an insert or update batch, in which sets of parameters are
 * applied to the same SQL and should be run in batch mode for performance.
 * 
 * @author rdoherty
 */
public class BatchUpdateExecutor extends PreparedStatementExecutor<Integer> {

  private static final Logger LOG = Logger.getLogger(BatchUpdateExecutor.class);

  private ArgumentBatch _argBatch;
  private int _numUpdates;

  public BatchUpdateExecutor(ArgumentBatch argBatch) {
    super(new Object[]{ }, null);
    _argBatch = argBatch;
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
    LOG.debug(new StringBuilder("Writing batch ").append(batchNumber)
        .append(" (").append(batchSize).append(" records) took ")
        .append(currentBatchTime).append(" ms. Cumulative batch execution time: ")
        .append(_lastExecutionTime).append(" ms").toString());
    for (int count : numUpdatesArray) {
      _numUpdates += count;
    }
  }

  @Override public Integer handleResult() throws SQLException, SQLRunnerException {
    return _numUpdates;
  }

  @Override
  public String getParamsToString() {
    return "{ batch of argument sets }";
  }
}
