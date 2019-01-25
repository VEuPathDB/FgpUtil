package org.gusdb.fgputil;

import org.gusdb.fgputil.Tuples.TwoTuple;

public class Range<T extends Comparable<T>> extends TwoTuple<T,T> {

  public Range(T min, T max) {
    super(min, max);
    if (min != null && max != null && min.compareTo(max) > 0) {
      throw new IllegalArgumentException("Min value cannot be greater than Max value.");
    }
  }

  public boolean hasMin() { return getMin() != null; }
  public boolean hasMax() { return getMax() != null; }

  public T getMin() { return getFirst(); }
  public T getMax() { return getSecond(); }

}
