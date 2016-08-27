package org.gusdb.fgputil.functional;

/**
 * Static class provides basic functional interfaces and true and false predicates
 * 
 * @author rdoherty
 */
public class FunctionalInterfaces {

  private FunctionalInterfaces(){}

  /**
   * Defines a single-argument function
   * 
   * @param <T> type of function input
   * @param <S> type of function output
   */
  public interface Function<T,S> {
    /**
     * Applies the function to the given input and returns output
     * 
     * @param obj input to function
     * @return result of function
     */
    public S apply(T obj);
  }

  /**
   * Defines a single-argument predicate (function that returns a boolean)
   *
   * @param <T> type of predicate input
   */
  public interface Predicate<T> {
    /**
     * Tests the given input against the predicate and returns whether the
     * passed input passes the test.
     * 
     * @param obj object to test
     * @return true if object passes, else false
     */
    public boolean test(T obj);
  }

  /**
   * Aggregates a set of input values into a single output
   *
   * @param <T> type of input values
   * @param <S> type of result
   */
  public interface Reducer<T, S> {
    /**
     * Returns an initial result based on the passed input value
     * 
     * @param obj object to evaluate
     * @return initial result
     */
    public S reduce(T obj);
    /**
     * Returns an aggregate result by combining the incoming value with that
     * produced by evaluating the passed object
     * 
     * @param obj object to evaluate
     * @param incomingValue previous result value
     * @return revised result
     */
    public S reduce(T obj, S incomingValue);
  }

  /**
   * Typed predicate that always returns true.
   *
   * @param <T> type of object being evaluated
   */
  public static class TruePredicate<T> implements Predicate<T> {
    @Override public boolean test(T obj) { return true; }
  }

  /**
   * Typed predicate that always returns false.
   *
   * @param <T> type of object being evaluated
   */
  public static class FalsePredicate<T> implements Predicate<T> {
    @Override public boolean test(T obj) { return false; }
  }

  /**
   * Returns a predicate that tests whether an object is equal to
   * the passed object using the object's equal() method.
   * 
   * @param obj object
   * @return predicate to test equality to passed object
   */
  public static <T> Predicate<T> equalTo(final T obj) {
    return new Predicate<T>() {
      @Override public boolean test(T candidate) {
        return obj.equals(candidate);
      }
    };
  }

  /**
   * Returns a predicate that tests whether an object is not equal to
   * the passed object using the object's equal() method.
   * 
   * @param obj object
   * @return predicate to test inequality to passed object
   */
  public static <T> Predicate<T> notEqualTo(final T obj) {
    return new Predicate<T>() {
      @Override public boolean test(T candidate) {
        return !obj.equals(candidate);
      }
    };
  }
}
