package org.gusdb.fgputil;

import java.util.Optional;

public class Range<T extends Comparable<T>> {

  private Optional<T> _beginValue = Optional.empty();
  private boolean _isBeginInclusive = true;

  private Optional<T> _endValue = Optional.empty();
  private boolean _isEndInclusive = true;

  public Range() {
    this(Optional.empty(), Optional.empty());
  }

  public Range(T begin, T end) {
    this(Optional.ofNullable(begin), Optional.ofNullable(end));
  }

  public Range(Optional<T> begin, Optional<T> end) {
    _beginValue = begin;
    _endValue = end;
    if (hasBegin() && hasEnd() && begin.get().compareTo(end.get()) > 0) {
      throw new IllegalArgumentException("Range's begin value (" + begin +
          ") cannot be greater than its end value (" + end + ").");
    }
  }

  public boolean hasBegin() { return _beginValue.isPresent(); }
  public void setBegin(T begin) { _beginValue = Optional.ofNullable(begin); }
  public T getBegin() { return _beginValue.get(); }
  public Optional<T> getBeginOpt() { return _beginValue; }
  public void setBeginInclusive(boolean isInclusive) { _isBeginInclusive = isInclusive; }
  public boolean isBeginInclusive() { return _isBeginInclusive; }
  
  public boolean hasEnd() { return _endValue.isPresent(); }
  public void setEnd(T end) { _endValue = Optional.ofNullable(end); }
  public T getEnd() { return _endValue.get(); }
  public Optional<T> getEndOpt() { return _endValue; }
  public void setEndInclusive(boolean isInclusive) { _isEndInclusive = isInclusive; }
  public boolean isEndInclusive() { return _isEndInclusive; }

}
