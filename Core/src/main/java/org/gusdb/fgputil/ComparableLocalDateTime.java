package org.gusdb.fgputil;

import java.time.LocalDateTime;

public class ComparableLocalDateTime implements Comparable<ComparableLocalDateTime> {

  private final LocalDateTime _t;

  public ComparableLocalDateTime(LocalDateTime t) {
    _t = t;
  }

  public LocalDateTime get() {
    return _t;
  }

  /**
   * @return a negative integer, zero, or a positive integer as this object is
   * less than, equal to, or greater than the specified object, respectively.
   */
  @Override
  public int compareTo(ComparableLocalDateTime t) {
    return _t.isBefore(t.get()) ? -1 :
           _t.isAfter(t.get()) ? 1 :
           0;
  }
}
