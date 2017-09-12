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
   * @param <S> type of function input
   * @param <T> type of function output
   */
  public interface Function<S,T> {
    /**
     * Applies the function to the given input and returns output
     * 
     * @param obj input to function
     * @return result of function
     */
    public T apply(S obj);
  }

  /**
   * Defines a single-argument function that may throw an exception
   * 
   * @param <S> type of function input
   * @param <T> type of function output
   */
  public interface FunctionWithException<S,T> {
    /**
     * Applies the function to the given input and returns output
     * 
     * @param obj input to function
     * @return result of function
     * @throws Exception as needed
     */
    public T apply(S obj) throws Exception;
  }

  /**
   * Defines a single-argument function
   * 
   * @param <R> type of first function input
   * @param <S> type of second function input
   * @param <T> type of function output
   */
  public interface BinaryFunction<R,S,T> {
    /**
     * Applies the function to the given input and returns output
     * 
     * @param obj1 input to function
     * @param obj2 input to function
     * @return result of function
     */
    public T apply(R obj1, S obj2);
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
   * Defines a single-argument predicate (function that returns a boolean) that may throw an exception
   *
   * @param <T> type of predicate input
   */
  public interface PredicateWithException<T> {
    /**
     * Tests the given input against the predicate and returns whether the
     * passed input passes the test.
     * 
     * @param obj object to test
     * @return true if object passes, else false
     */
    public boolean test(T obj) throws Exception;
  }

  /**
   * Aggregates a set of input values into a single output
   *
   * @param <T> type of input values
   * @param <S> type of result
   */
  public interface Reducer<T, S> {
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
   * Performs a procedure that has no output and does not need parameters
   */
  public interface Procedure {
    public void perform();
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
   * Returns a predicate that negates the result of the passed predicate
   * for each input (i.e. if the passed predicate's test method returns
   * true, the returned predicate returns false, and vice versa).
   * 
   * @param predicate any predicate
   * @return predicate that negates results of the passed predicate
   */
  public static <T> Predicate<T> negate(final Predicate<T> predicate) {
    return new Predicate<T>() {
      @Override public boolean test(T candidate) {
        return !predicate.test(candidate);
      }
    };
  }
}
