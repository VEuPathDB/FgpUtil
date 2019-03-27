package org.gusdb.fgputil.functional;

import java.util.function.Predicate;

/**
 * Static class provides basic functional interfaces and true and false predicates
 *
 * @author rdoherty
 */
public class FunctionalInterfaces {

  private FunctionalInterfaces(){}

  /**
   * Defines a single-argument function that may throw an exception
   *
   * @param <S> type of function input
   * @param <T> type of function output
   */
  @FunctionalInterface
  public interface FunctionWithException<S,T> {
    /**
     * Applies the function to the given input and returns output
     *
     * @param obj input to function
     * @return result of function
     * @throws Exception as needed
     */
    T apply(S obj) throws Exception;
  }

  /**
   * Defines a two-argument function that may throw an exception
   *
   * @param <R> type of first function input
   * @param <S> type of second function input
   * @param <T> type of function output
   */
  @FunctionalInterface
  public interface BiFunctionWithException<R,S,T> {
    /**
     * Applies the function to the given input and returns output
     *
     * @param obj1 input to function
     * @param obj2 input to function
     * @return result of function
     * @throws Exception as needed
     */
    T apply(R obj1, S obj2) throws Exception;
  }

  /**
   * Defines a three-argument function
   *
   * @param <R> type of first function input
   * @param <S> type of second function input
   * @param <T> type of third function input
   * @param <U> type of function output
   */
  @FunctionalInterface
  public interface TriFunction<R,S,T,U> {
    /**
     * Applies the function to the given input and returns output
     *
     * @param obj1 input to function
     * @param obj2 input to function
     * @param obj3 input to function
     * @return result of function
     */
    U apply(R obj1, S obj2, T obj3);
  }

  /**
   * Defines a no-argument function that may throw an exception
   *
   * @param <T> type of function output
   */
  @FunctionalInterface
  public interface SupplierWithException<T> {
    /**
     * Applies the function to produce an object of type T
     *
     * @return result of function
     * @throws Exception if something goes wrong
     */
    T get() throws Exception;
  }

  /**
   * Defines a no-argument function that may throw a defined exception
   *
   * @param <E> type of the expected checked exception
   * @param <T> type of function output
   */
  @FunctionalInterface
  public interface CheckedSupplier<E extends Throwable, T> {
    T get() throws E;
  }

  /**
   * Defines a consumer that may throw an exception
   *
   * @param <T> type of object being consumed
   */
  public interface ConsumerWithException<T> {
    /**
     * Consumes an object of type T
     *
     * @param obj object to consume
     * @throws Exception if something goes wrong
     */
    void accept(T obj) throws Exception;
  }

  /**
   * Defines a single-argument predicate (function that returns a boolean) that may throw an exception
   *
   * @param <T> type of predicate input
   */
  @FunctionalInterface
  public interface PredicateWithException<T> {
    /**
     * Tests the given input against the predicate and returns whether the
     * passed input passes the test.
     *
     * @param obj object to test
     * @return true if object passes, else false
     */
    boolean test(T obj) throws Exception;
  }

  /**
   * Aggregates a set of input values into a single output
   *
   * @param <S> type of input values
   * @param <T> type of result
   */
  @FunctionalInterface
  public interface Reducer<S, T> {
    /**
     * Returns an aggregate result by combining the incoming value with that
     * produced by evaluating the passed object
     *
     * @param accumulator previous result value
     * @param next object to evaluate
     * @return revised result
     */
    T reduce(T accumulator, S next);
  }

  /**
   * Aggregates a set of input values into a single output and may throw an exception
   *
   * @param <S> type of input values
   * @param <T> type of result
   */
  @FunctionalInterface
  public interface ReducerWithException<S, T> {
    /**
     * Returns an aggregate result by combining the incoming value with that
     * produced by evaluating the passed object
     *
     * @param accumulator previous result value
     * @param next object to evaluate
     * @return revised result
     */
    T reduce(T accumulator, S next) throws Exception;
  }

  /**
   * Performs a procedure that has no output and does not need parameters
   */
  @FunctionalInterface
  public interface Procedure {
    void perform();
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
    return obj::equals;
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
    return candidate -> !predicate.test(candidate);
  }
}
