package org.gusdb.fgputil.distribution;

import java.util.stream.Stream;
import org.gusdb.fgputil.Tuples.TwoTuple;

public abstract class AbstractDistribution {

  public enum ValueSpec {
    COUNT, PROPORTION
  }

  // provides application-specific production of base distribution data
  private final DistributionStreamProvider _streamProvider;

  // used to tailor the response to either count or proportion values
  protected final ValueSpec _valueSpec;

  /**
   * Build a distribution result from the passed stream; unique record count is provided
   * @param distributionStream stream of tuples for processing
   * @param recordCount number of records in the result
   * @return distribution result
   */
  protected abstract DistributionResult processDistributionStream(
      long recordCount, Stream<TwoTuple<String, Long>> distributionStream);

  public AbstractDistribution(DistributionStreamProvider streamProvider, ValueSpec valueSpec) {
    _streamProvider = streamProvider;
    _valueSpec = valueSpec;
  }

  public DistributionResult generateDistribution() {
    try(
      // create a stream of distribution tuples converted from a database result or other source
      Stream<TwoTuple<String, Long>> distributionStream = _streamProvider.getDistributionStream();
    ) {
      return processDistributionStream(_streamProvider.getRecordCount(), distributionStream);
    }
  }
}
