package org.gusdb.fgputil.db.runner;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;

import org.gusdb.fgputil.FormatUtil;
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
  private Integer[] _types;

  @Override
  public int getBatchSize() {
    return _batchSize;
  }

  public void setBatchSize(int batchSize) {
    _batchSize = batchSize;
  }

  @Override
  public Integer[] getParameterTypes() {
    return _types;
  }

  public void setParameterTypes(Integer[] types) {
    _types = types;
  }

  @Override
  public String toString() {
    StringBuilder out = new StringBuilder("ArgumentBatch of size ")
        .append(size()).append(" {").append(NL);
    for (Object[] args : this) {
      out.append("   ").append(FormatUtil.arrayToString(args)).append(NL);
    }
    return out.append("}").append(NL).toString();
  }
}
