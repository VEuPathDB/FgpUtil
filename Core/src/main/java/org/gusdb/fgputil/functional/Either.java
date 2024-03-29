package org.gusdb.fgputil.functional;

import static java.util.Objects.requireNonNull;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Either represents a value that MUST be one of 2 options, left or right.
 *
 * @param <L>
 *   Left type, conventionally an error case.
 * @param <R>
 *   Right type, conventionally a value case.
 */
public class Either<L, R> {

  private final L _left;
  private final R _right;

  /**
   * Construct a new either out of exactly 1 value and 1 null.
   *
   * @param left
   *   Left value.  By convention, an Either's left value is a failure case.
   * @param right
   *   Right value.  By convention an Either's right value is a success case.
   *
   * @throws IllegalArgumentException
   *   if both params are non-null or if both params are null
   */
  public Either(final L left, final R right) {
    if (Objects.isNull(left) == Objects.isNull(right)) {
      throw new IllegalArgumentException("either must have exactly 1 value");
    }
    _left = left;
    _right = right;
  }

  /**
   * @return whether or not this is an Either of {@link L}
   */
  public boolean isLeft() {
    return _left != null;
  }

  /**
   * @return whether or not this is an Either of {@link R}
   */
  public boolean isRight() {
    return _right != null;
  }

  /**
   * @return an option of an {@link L} value.
   */
  public Optional<L> left() {
    return Optional.ofNullable(_left);
  }

  /**
   * @return an option of an {@link R} value.
   */
  public Optional<R> right() {
    return Optional.ofNullable(_right);
  }

  /**
   * Unwrap the left value of this Either.
   *
   * @return The left value of this Either.
   *
   * @throws NoSuchElementException
   *   if this is not a left either.
   */
  public L getLeft() {
    if (_left == null)
      throw new NoSuchElementException("left value not present");

    return _left;
  }

  /**
   * Unwrap the right value of this Either.
   *
   * @return The right value of this Either.
   *
   * @throws NoSuchElementException
   *   if this is not a right either.
   */
  public R getRight() {
    if (_right == null)
      throw new NoSuchElementException("right value not present");

    return _right;
  }

  /**
   * Executes the given consumer if this is a left either.
   *
   * @param fn
   *   Consumer of {@link L} that will be called only if this is a left either.
   *
   * @return this Either.
   */
  public Either<L, R> ifLeft(final Consumer<L> fn) {
    if (isLeft())
      fn.accept(_left);

    return this;
  }

  /**
   * Executes the given consumer if this is a right either.
   *
   * @param fn
   *   Consumer of {@link R} that will be called only if this is a right
   *   either.
   *
   * @return this Either.
   */
  public Either<L, R> ifRight(final Consumer<R> fn) {
    if (isRight())
      fn.accept(_right);

    return this;
  }

  /**
   * Returns the left value if present, else throws the throwable supplied by
   * the given method.
   *
   * @param fn
   *   Throwable supplier
   * @param <E>
   *   Type of Throwable
   *
   * @return left value
   *
   * @throws E
   *   if this is not a left either.
   */
  public <E extends Throwable> L leftOrElseThrow(final Supplier<E> fn)
  throws E {
    return left().orElseThrow(fn);
  }

  /**
   * Returns the left value if present, else throws the throwable supplied by
   * the given function.  The right value will be passed to the function.
   *
   * @param fn
   *   Throwable supplier
   * @param <E>
   *   Type of Throwable
   *
   * @return left value
   *
   * @throws E
   *   if this is not a left either.
   */
  public <E extends Throwable> L leftOrElseThrowWithRight(Function<R,E> fn) throws E {
    return left().orElseThrow(() -> fn.apply(_right));
  }

  /**
   * Returns the right value if present, else throws the throwable supplied by
   * the given method.
   *
   * @param fn
   *   Throwable supplier
   * @param <E>
   *   Type of Throwable
   *
   * @return right value
   *
   * @throws E
   *   if this is not a right either.
   */
  public <E extends Throwable> R rightOrElseThrow(final Supplier<E> fn)
  throws E {
    return right().orElseThrow(fn);
  }

  /**
   * Returns a new Either of a new left type {@link N}.  The value of type L
   * will be mapped to type N using the given input function.
   *
   * @param fn
   *   Function to map type L of the current Either to the new type N.
   * @param <N>
   *   Left type for the new either to be returned by this method.
   *
   * @return A new either with a left type of {@link N} and a right type of
   * {@link R}.
   */
  public <N> Either<N, R> mapLeft(final Function<L, N> fn) {
    return isLeft()
      ? Either.left(fn.apply(getLeft()))
      : Either.right(getRight());
  }

  /**
   * Returns a new Either of a new right type {@link N}.  The value of type R
   * will be mapped to type N using the given input function.
   *
   * @param fn
   *   Function to map type R of the current Either to the new type N.
   * @param <N>
   *   Right type for the new either to be returned by this method.
   *
   * @return A new either with a left type of {@link L} and a right type of
   * {@link N}.
   */
  public <N> Either<L, N> mapRight(final Function<R, N> fn) {
    return isRight()
      ? Either.right(fn.apply(getRight()))
      : Either.left(getLeft());
  }

  /**
   * Construct a left either.
   *
   * @param val
   *   Left value
   * @param <L>
   *   Left value type
   * @param <R>
   *   Right value type
   *
   * @return An either wrapping val.
   *
   * @throws NullPointerException
   *   if val is null.
   */
  public static <L, R> Either<L, R> left(final L val) {
    return new Either<>(requireNonNull(val), null);
  }

  /**
   * Construct a right either.
   *
   * @param val
   *   Right value
   * @param <L>
   *   Left value type
   * @param <R>
   *   Right value type
   *
   * @return An either wrapping val.
   *
   * @throws NullPointerException
   *   if val is null.
   */
  public static <L, R> Either<L, R> right(final R val) {
    return new Either<>(null, requireNonNull(val));
  }

}
