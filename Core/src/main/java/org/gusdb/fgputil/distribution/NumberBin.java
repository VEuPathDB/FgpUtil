package org.gusdb.fgputil.distribution;

import static org.gusdb.fgputil.distribution.NumberBinDistribution.isLessThan;

public class NumberBin<T extends Number & Comparable<T>> implements AbstractBinDistribution.Bin<T>{

  protected final T _start; // inclusive
  protected final T _end; // exclusive

  private long _binCount = 0;

  public NumberBin(T start, T end) {
    _start = start;
    _end = end;
  }

  @Override
  public boolean startsAfter(T value) {
    return isLessThan(value, _start);
  }

  @Override
  public boolean accept(T value, Long count) {
    // start inclusive, end exclusive
    if (!isLessThan(value, _start) &&
        (isLessThan(value, _end))) {
      _binCount += count;
      return true;
    }
    return false;
  }

  @Override
  public HistogramBin toHistogramBin() {
    HistogramBin bin = new HistogramBin();
    bin.setBinStart(String.valueOf(_start));
    bin.setBinEnd(String.valueOf(_end));
    bin.setBinLabel("[" + bin.getBinStart() + "," + bin.getBinEnd() + ")");
    bin.setValue(_binCount);
    return bin;
  }
}
