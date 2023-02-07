package org.gusdb.fgputil;

import java.util.Map.Entry;

public class Tuples {

  private Tuples() {}

  public static class TwoTuple<S,T> implements Entry<S,T> {

    protected S _first;
    protected T _second;

    /** Constructor initializes both values to null */
    public TwoTuple() { }

    public TwoTuple(S first, T second) {
      set(first, second);
    }

    public void set(S first, T second) {
      _first = first;
      _second = second;
    }

    public S getFirst() { return _first; }
    public T getSecond() { return _second; }

    // methods required by Entry
    @Override public S getKey() { return _first; }
    @Override public T getValue() { return _second; }
    @Override public T setValue(T value) { T t = _second; _second = value; return t; }

    @Override
    public boolean equals(Object o) {
      if (o instanceof Entry) {
        Entry<?,?> t = (Entry<?,?>)o;
        boolean firstEqual = _first == null ? t.getKey() == null : _first.equals(t.getKey());
        boolean secondEqual = _second == null ? t.getValue() == null : _second.equals(t.getValue());
        return firstEqual && secondEqual;
      }
      return false;
    }

    @Override
    public int hashCode() {
      return (String.valueOf(_first) + String.valueOf(_second)).hashCode();
    }
  }

  public static class ThreeTuple<R,S,T> extends TwoTuple<R,S> {

    private T _third;

    /** Constructor initializes all values to null */
    public ThreeTuple() { }

    public ThreeTuple(R first, S second, T third) {
      super(first, second);
      _third = third;
    }
    
    public void set(R first, S second, T third) {
      set(first, second);
      _third = third;
    }

    public T getThird() { return _third; }

    @Override
    public boolean equals(Object o) {
      if (o instanceof ThreeTuple) {
        ThreeTuple<?,?,?> t = (ThreeTuple<?,?,?>)o;
        return super.equals(o) && _third == null ? t._third == null : _third.equals(t._third);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return (String.valueOf(_first) + String.valueOf(_second) + String.valueOf(_third)).hashCode();
    }
  }
}
