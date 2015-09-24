package org.gusdb.fgputil.functional;

public class FunctionalInterfaces {

  public interface Function<T,S> { public S apply(T obj); }

  public interface Reducer<T, S> {
    public S reduce(T obj);
    public S reduce(T obj, S incomingValue);
  }

  public interface Predicate<T>  { public boolean test(T obj); }

  public static class TruePredicate<T> implements Predicate<T> {
    @Override public boolean test(T obj) { return true; }
  }

  public static class FalsePredicate<T> implements Predicate<T> {
    @Override public boolean test(T obj) { return false; }
  }
}
