package org.gusdb.fgputil;

public class Tuples {

  private Tuples() {}

  public static class TwoTuple<S,T> {

    private S _first;
    private T _second;

    public TwoTuple(S first, T second) {
      set(first, second);
    }

    public void set(S first, T second) {
      _first = first;
      _second = second;
    }

    public S getFirst() { return _first; }
    public T getSecond() { return _second; }
  }

  public static class ThreeTuple<R,S,T> extends TwoTuple<R,S> {

    private T _third;

    public ThreeTuple(R first, S second, T third) {
      super(first, second);
      _third = third;
    }
    
    public void set(R first, S second, T third) {
      set(first, second);
      _third = third;
    }

    public T getThird() { return _third; }
  }
}
