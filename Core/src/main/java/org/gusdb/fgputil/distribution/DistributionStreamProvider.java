package org.gusdb.fgputil.distribution;

import java.util.stream.Stream;
import org.gusdb.fgputil.Tuples.TwoTuple;

public interface DistributionStreamProvider {

  /**
   * @return stream of [value, countOfValue] tuples
   * NOTE: this stream must be closed by the caller
   */
  Stream<TwoTuple<String, Long>> getDistributionStream();

  /**
   * @return number of unique records; may or may not be the same as unique values
   * since some records may have no values and others may have >1 depending on
   * the data model
   */
  long getRecordCount();

}
