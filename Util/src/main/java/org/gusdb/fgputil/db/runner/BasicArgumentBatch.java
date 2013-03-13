package org.gusdb.fgputil.db.runner;

import java.util.ArrayList;

import org.gusdb.fgputil.db.runner.SQLRunner.ArgumentBatch;

/**
 * Basic implementation of ArgumentBatch.  Allows user to set the batch size and
 * add parameters via the extended ArrayList.
 * 
 * @author rdoherty
 */
public class BasicArgumentBatch extends ArrayList<Object[]> implements ArgumentBatch {

  private static final long serialVersionUID = 1L;

  public static final int DEFAULT_BATCH_SIZE = 100;
  
  private int _batchSize = DEFAULT_BATCH_SIZE;

  @Override
  public int getBatchSize() {
    return _batchSize;
  }
  public void setBatchSize(int batchSize) {
    _batchSize = batchSize;
  }

}
