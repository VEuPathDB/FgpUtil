package org.gusdb.fgputil.distribution;

import java.util.Objects;
import java.util.function.Supplier;
import java.math.BigDecimal;

public class FloatingPointBinDistribution extends NumberBinDistribution<Double> {

  public FloatingPointBinDistribution(DistributionStreamProvider streamProvider, ValueSpec valueSpec, NumberBinSpec binSpec) {
    super(streamProvider, valueSpec, binSpec);
  }

  @Override
  protected Double sum(Double a, Double b) {
    // use BigDecimal to prevent "precision drift" errors
    return new BigDecimal(Double.toString(a))
           .add(new BigDecimal(Double.toString(b)))
           .doubleValue();
  }

  @Override
  protected void validateBinWidth(Double binWidth) throws IllegalArgumentException {
    if (binWidth == null) {
      throw new IllegalArgumentException("Bin width cannot be null.");
    }
    if (binWidth <= 0) {
      throw new IllegalArgumentException("Bin width must be a positive number.");
    }
  }

  @Override
  protected Double getTypedObject(String objectName, Object value, ValueSource source) {
    Supplier<RuntimeException> exSupplier = () -> { switch(source) {
      case CONFIG: return new IllegalArgumentException(objectName + " must be a number value.");
      case DB: return new RuntimeException("Value in column " + objectName + " is not a number.");
      default: return null;
    }};
    Objects.requireNonNull(value);
    if (value instanceof Number) {
      return ((Number)value).doubleValue();
    }
    if (value instanceof String) {
      try {
        return Double.parseDouble((String)value);
      }
      catch (NumberFormatException e) {
        throw exSupplier.get();
      }
    }
    throw exSupplier.get();
  }

  @Override
  protected StatsCollector<Double> getStatsCollector() {
    return new StatsCollector<>() {

      private double _sumOfValues = 0;

      @Override
      public void accept(Double value, Long count) {
        super.accept(value, count);
        _sumOfValues += (count * value);
      }

      @Override
      public HistogramStats toHistogramStats(long subsetEntityCount, long missingCasesCount) {
        HistogramStats stats = super.toHistogramStats(subsetEntityCount, missingCasesCount);
        if (isDataPresent()) {
          stats.setSubsetMean(_sumOfValues / stats.getNumVarValues());
        }
        return stats;
      }
    };
  }

}
