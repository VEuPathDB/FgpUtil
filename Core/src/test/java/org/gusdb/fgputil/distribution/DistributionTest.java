package org.gusdb.fgputil.distribution;

import java.math.BigDecimal;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.distribution.AbstractDistribution.ValueSpec;
import org.gusdb.fgputil.distribution.NumberBinDistribution.NumberBinSpec;
import org.junit.Test;

import org.junit.Assert;

public class DistributionTest {

  // try bin spec configs with a variety of different types
  private static NumberBinSpec getNumberBinSpec(Object displayRangeMin) {
    return new NumberBinSpec() {

      @Override
      public Object getDisplayRangeMin() {
        return displayRangeMin;
      }
  
      @Override
      public Object getDisplayRangeMax() {
        return Integer.valueOf(10);
      }
  
      @Override
      public Object getBinSize() {
        return 3;
      }
    };
  }

  private static DistributionStreamProvider DATA = new DistributionStreamProvider() {

    private static final int SIZE = 100;

    @Override
    public Stream<TwoTuple<String, Long>> getDistributionStream() {
      Random rnd = new Random();
      return IntStream
          .range(0, SIZE - 1)
          .mapToObj(i -> new TwoTuple<String,Long>(
              String.valueOf(i), rnd.nextInt(5) + 1L));
    }

    @Override
    public long getRecordCount() {
      return SIZE;
    }
  };

  @Test
  public void testIntegerDistribution() {
    testIntegerDistribution(Integer.valueOf(1));
    testIntegerDistribution(Long.valueOf(1));
    testIntegerDistribution(BigDecimal.valueOf(1));
    testIntegerDistribution("1");
    //testIntegerDistribution("1.0"); // will break as it should!
  }

  public void testIntegerDistribution(Object displayRangeMin) {
    DistributionResult result = new IntegerBinDistribution(
        DATA,
        ValueSpec.COUNT,
        getNumberBinSpec(displayRangeMin)
    ).generateDistribution();

    // make sure distribution behaves the same regardless of input range data type
    Assert.assertEquals(4, result.getHistogramData().size());
  }

}
