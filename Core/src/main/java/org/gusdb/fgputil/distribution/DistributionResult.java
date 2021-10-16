package org.gusdb.fgputil.distribution;

import java.util.List;
import org.gusdb.fgputil.Tuples;

public class DistributionResult extends Tuples.TwoTuple<List<HistogramBin>, HistogramStats> {

  public DistributionResult(List<HistogramBin> histogram, HistogramStats stats) {
    super(histogram, stats);
  }

  public List<HistogramBin> getHistogramData() {
    return getFirst();
  }

  public HistogramStats getStatistics() {
    return getSecond();
  }

}
