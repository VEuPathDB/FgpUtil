package org.gusdb.fgputil;

import org.gusdb.fgputil.Tuples.TwoTuple;

public class Range<T extends Comparable<T>> extends TwoTuple<T,T> {

  public static <T> T empty() { return null; }

  public Range(T begin, T end) {
    super(begin, end);
    if (hasBegin() && hasEnd() && begin.compareTo(end) > 0) {
      throw new IllegalArgumentException("Range's begin value cannot be greater than its end value.");
    }
  }

  public boolean hasBegin() { return getBegin() != empty(); }
  public void setBegin(T begin) { set(begin, getEnd()); }
  public T getBegin() { return getFirst(); }
  
  public boolean hasEnd() { return getEnd() != empty(); }
  public void setEnd(T end) { set(getBegin(), end); }
  public T getEnd() { return getSecond(); }

}
