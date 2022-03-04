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
   * @param omitHistogram whether to exclude histogram property from results (sometimes only statistics are required
   * @return distribution result
   */
  protected abstract DistributionResult processDistributionStream(
      long recordCount, Stream<TwoTuple<String, Long>> distributionStream, boolean omitHistogram);

  public AbstractDistribution(DistributionStreamProvider streamProvider, ValueSpec valueSpec) {
    _streamProvider = streamProvider;
    _valueSpec = valueSpec;
  }

  /**
   * Generates a distribution result with histogram from the given configuration
   *
   * This is identical to a call to generateDistribution(false);
   *
   * @return distribution result
   */
  public DistributionResult generateDistribution() {
    return generateDistribution(false);
  }

  /**
   * Generates a distribution result from the given configuration
   *
   * @param omitHistogram whether to omit the histogram property from the result (sometimes only statistics are required)
   * @return distribution result
   */
  public DistributionResult generateDistribution(boolean omitHistogram) {
    try(
      // create a stream of distribution tuples converted from a database result or other source
      Stream<TwoTuple<String, Long>> distributionStream = _streamProvider.getDistributionStream();
    ) {
      return processDistributionStream(_streamProvider.getRecordCount(), distributionStream, omitHistogram);
    }
  }
}
