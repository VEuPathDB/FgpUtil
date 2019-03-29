package org.gusdb.fgputil.functional;

import org.gusdb.fgputil.functional.FunctionalInterfaces.CheckedSupplier;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * A specialized Either type for conditions where the expected result is either
 * a value or an error.
 *
 * @param <E> Throwable instance
 * @param <V> Success value
 */
public class Result <E extends Throwable, V> extends Either <E, V> {

  /**
   * Construct a new either out of exactly 1 value and 1 null.
   *
   * @param error Error value.
   * @param value Success value.
   *
   * @throws IllegalArgumentException if both params are non-null or if both
   *         params are null
   */
  public Result(final E error, final V value) {
    super(error, value);
  }

  /**
   * Unwrap the error value of this Result.
   *
   * @return The error value of this Result.
   *
   * @throws NoSuchElementException if this is not an error result.
   */
  public E getError() {
    return getLeft();
  }

  /**
   * @return an option of an {@link E} value.
   */
  public Optional<E> error() {
    return left();
  }

  /**
   * Unwrap the success value of this Result.
   *
   * @return The success value of this Result.
   *
   * @throws NoSuchElementException if this is not a success result.
   */
  public V getValue() {
    return getRight();
  }

  /**
   * @return an option of an {@link V} value.
   */
  public Optional<V> value() {
    return right();
  }

  /**
   * @return whether or not this result is an error.
   */
  public boolean isError() {
    return isLeft();
  }

  /**
   * @return whether or not this result is a value.
   */
  public boolean isValue() {
    return isRight();
  }

  /**
   * Returns the success value if present, or throws the exception supplied by
   * the given function.
   *
   * @param fn   Function provides a Throwable instance to be thrown
   * @param <NE> Return type for fn
   *
   * @return success value
   *
   * @throws NE if this is not a right either.
   */
  public <NE extends Throwable> V valueOrElseThrow(final Supplier<NE> fn)
  throws NE {
    return super.rightOrElseThrow(fn);
  }

  /**
   * Returns the success value if present, else applies the given function to
   * the contained error.
   *
   * @param fn   Function that takes the contained throwable and returns E
   * @param <NE> Return type for fn
   *
   * @return success value
   *
   * @throws NE if this is not a right either.
   */
  public <NE extends Throwable> V valueOrElseThrow(final Function<E, NE> fn)
  throws NE {
    return super.rightOrElseThrow(() -> fn.apply(getLeft()));
  }

  /**
   * Returns the sucess value if present, or else throws the wrapped error.
   *
   * @return the wrapped success value if present.
   *
   * @throws E the wrapped exception if value is not present.
   */
  public V valueOrElseThrow() throws E {
    return right().orElseThrow(this::getLeft);
  }

  /**
   * Map the wrapped exception of type {@link E} (if that error exists) to type
   * {@code N}.
   *
   * @param fn mapping function
   * @param <N> new error type
   *
   * @return a new result of either the existing value or the new error type.
   */
  public <N extends Throwable> Result<N, V> mapError(final Function<E, N> fn) {
    return (Result<N, V>) super.mapLeft(fn);
  }

  /**
   * Map the wrapped value of type {@link V} (if that value exists) to type
   * {@code N}.
   *
   * @param fn mapping function
   * @param <N> new value type
   *
   * @return a new result of either the existing error or the new value type.
   */
  public <N> Result<E, N> mapValue(final Function<V, N> fn) {
    return (Result<E, N>) super.mapRight(fn);
  }

  /**
   * Flat map over the wrapped value of type {@link V} (if a value exists) and
   * map it to a result of type {@link N}.
   *
   * @param fn mapping function
   * @param <N> new value type
   *
   * @return A result wrapping the existing error or the new value result of
   * {@code fn};
   */
  public <N> Result<E, N> flatMapValue(
    final Function<V, Result<E, N>> fn
  ) {
    return isValue()
      ? fn.apply(getValue())
      : Result.error(getError());
  }

  /**
   * Performs the given action on the wrapped value if that value exists.
   *
   * @param fn action to perform on the wrapped value
   *
   * @return this {@code Result}
   */
  public Result<E, V> withValue(final Consumer<V> fn) {
    if (isValue())
      fn.accept(getValue());
    return this;
  }

  /**
   * Construct an error result.
   *
   * @param val Exception value
   * @param <L> Exception value type
   * @param <R> Success value type
   *
   * @return A Result wrapping val.
   *
   * @throws NullPointerException if val is null.
   */
  public static < L extends Throwable, R > Result < L, R > error(final L val) {
    return new Result<>(Objects.requireNonNull(val), null);
  }

  /**
   * Construct a success result.
   *
   * @param val success value
   * @param <L> Exception value type
   * @param <R> Success value type
   *
   * @return A Result wrapping val.
   *
   * @throws NullPointerException if val is null.
   */
  public static < L extends Throwable, R > Result < L, R > value(final R val) {
    return new Result<>(null, Objects.requireNonNull(val));
  }

  /**
   * Executes and wraps the result of function that will produce a value of type
   * {@code R} and may throw an exception of type {@link L}.
   *
   * @param fn value supplier
   * @param <L> Exception value type
   * @param <R> Success value type
   *
   * @return the wrapped result of the execution of the given function.
   */
  @SuppressWarnings("unchecked") // TODO: address runtime exceptions thrown by value()
  public static < L extends Throwable, R > Result < L, R > of(
    final CheckedSupplier< L, R > fn
  ) {
    try {
      return value(fn.get());
    }
    catch (Throwable e) {
      return error((L) e);
    }
  }
}
