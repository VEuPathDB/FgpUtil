package org.gusdb.fgputil.distribution;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.iterator.IteratorUtil;

public class DiscreteDistribution extends AbstractDistribution {

  public DiscreteDistribution(DistributionStreamProvider streamProvider, ValueSpec valueSpec) {
    super(streamProvider, valueSpec);
  }

  @Override
  protected DistributionResult processDistributionStream(long subsetEntityCount, Stream<TwoTuple<String,Long>> distributionStream) {
    List<HistogramBin> bins = new ArrayList<>();
    long distinctValueCount = 0;
    long totalValueCount = 0;
    long missingCasesCount = 0;
    for (TwoTuple<String,Long> tuple : IteratorUtil.toIterable(distributionStream.iterator())) {
      if (tuple.getKey() == null) {
        missingCasesCount = tuple.getValue();
        continue;
      }
      HistogramBin bin = new HistogramBin();
      bin.setBinStart(tuple.getKey());
      bin.setBinEnd(tuple.getKey());
      bin.setBinLabel(tuple.getKey());
      bin.setValue(tuple.getValue());
      distinctValueCount++;
      totalValueCount += tuple.getValue();
      bins.add(bin);
    }
    HistogramStats stats = new HistogramStats();
    stats.setSubsetSize(subsetEntityCount);
    stats.setNumMissingCases(missingCasesCount);
    stats.setNumDistinctValues(distinctValueCount);
    stats.setNumVarValues(totalValueCount);
    stats.setNumDistinctEntityRecords(subsetEntityCount - missingCasesCount);
    return new DistributionResult(bins, stats);
  }
}
