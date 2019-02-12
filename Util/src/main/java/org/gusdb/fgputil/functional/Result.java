package org.gusdb.fgputil.functional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
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
  public Result(E error, V value) {
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
  public <NE extends Throwable> V valueOrElseThrow(Supplier<NE> fn) throws NE {
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
  public <NE extends Throwable> V valueOrElseThrow(Function<E, NE> fn) throws NE {
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
    return right().orElseThrow(() -> getLeft());
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
  public static < L extends Throwable, R > Result < L, R > error(L val) {
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
  public static < L extends Throwable, R > Result < L, R > value(R val) {
    return new Result<>(null, Objects.requireNonNull(val));
  }
}
