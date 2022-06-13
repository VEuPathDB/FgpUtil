package org.gusdb.fgputil.distribution;

import java.util.Objects;
import java.util.function.Supplier;

public class IntegerBinDistribution extends NumberBinDistribution<Long> {

  public IntegerBinDistribution(DistributionStreamProvider streamProvider, ValueSpec valueSpec, NumberBinSpec binSpec) {
    super(streamProvider, valueSpec, binSpec);
  }

  @Override
  protected Long sum(Long a, Long b) {
    return a + b;
  }

  @Override
  protected void validateBinWidth(Long binWidth) throws IllegalArgumentException {
    if (binWidth == null) {
      throw new IllegalArgumentException("Bin width cannot be null.");
    }
    if (binWidth <= 0) {
      throw new IllegalArgumentException("Bin width must be a positive integer.");
    }
  }

  @Override
  protected Long getTypedObject(String objectName, Object value, ValueSource source) {
    Supplier<RuntimeException> exSupplier = () -> { switch(source) {
      case CONFIG: return new IllegalArgumentException(objectName + " must be an integer value.");
      case DB: return new RuntimeException("Value in column " + objectName + " is not an integer.");
      default: return null;
    }};
    Objects.requireNonNull(value);
    if (value instanceof Number) {
      return ((Number)value).longValue();
    }
    if (value instanceof String) {
      try {
        return Long.parseLong((String)value);
      }
      catch (NumberFormatException e) {
        throw exSupplier.get();
      }
    }
    throw exSupplier.get();
  }

  @Override
  protected StatsCollector<Long> getStatsCollector() {
    return new StatsCollector<>() {

      private Long _sumOfValues = 0L;

      @Override
      public void accept(Long value, Long count) {
        super.accept(value, count);
        _sumOfValues += (count * value);
      }

      @Override
      public HistogramStats toHistogramStats(long subsetEntityCount, long missingCasesCount) {
        HistogramStats stats = super.toHistogramStats(subsetEntityCount, missingCasesCount);
        if (isDataPresent()) {
          stats.setSubsetMean(_sumOfValues.doubleValue() / stats.getNumVarValues());
        }
        return stats;
      }
    };
  }

}
