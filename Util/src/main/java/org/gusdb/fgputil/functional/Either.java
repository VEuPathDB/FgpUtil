package org.gusdb.fgputil.functional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Either represents a value that MUST be one of 2 options, left or right.
 *
 * @param <L> Left type, conventionally an error case.
 * @param <R> Right type, conventionally a value case.
 */
public class Either < L, R > {
  private final L left;
  private final R right;

  /**
   * Construct a new either out of exactly 1 value and 1 null.
   *
   * @param left  Left value.  By convention, an Either's left value is a
   *              failure case.
   * @param right Right value.  By convention an Either's right value is a
   *              success case.
   *
   * @throws IllegalArgumentException if both params are non-null or if both
   * params are null
   */
  public Either(L left, R right) {
    if (Objects.isNull(left) ^ Objects.isNull(right))
      throw new IllegalArgumentException("either must have exactly 1 value");

    this.left = left;
    this.right = right;
  }

  /**
   * @return whether or not this is an Either of {@link L}
   */
  public boolean isLeft() {
    return left != null;
  }

  /**
   * @return whether or not this is an Either of {@link R}
   */
  public boolean isRight() {
    return right != null;
  }

  /**
   * @return an option of an {@link L} value.
   */
  public Optional<L> left() {
    return Optional.ofNullable(left);
  }

  /**
   * @return an option of an {@link R} value.
   */
  public Optional<R> right() {
    return Optional.ofNullable(right);
  }

  /**
   * Unwrap the left value of this Either.
   *
   * @return The left value of this Either.
   *
   * @throws NoSuchElementException if this is not a left either.
   */
  public L getLeft() {
    if (left == null)
      throw new NoSuchElementException("left value not present");

    return left;
  }

  /**
   * Unwrap the right value of this Either.
   *
   * @return The right value of this Either.
   *
   * @throws NoSuchElementException if this is not a right either.
   */
  public R getRight() {
    if (right == null)
      throw new NoSuchElementException("right value not present");

    return right;
  }

  /**
   * Executes the given consumer if this is a left either.
   *
   * @param fn Consumer of {@link L} that will be called only if this is a left
   *           either.
   *
   * @return this Either.
   */
  public Either < L, R > ifLeft(Consumer<L> fn) {
    if (isLeft())
      fn.accept(left);

    return this;
  }

  /**
   * Executes the given consumer if this is a right either.
   *
   * @param fn Consumer of {@link R} that will be called only if this is a right
   *           either.
   *
   * @return this Either.
   */
  public Either < L, R > ifRight(Consumer<R> fn) {
    if (isRight())
      fn.accept(right);

    return this;
  }

  /**
   * Construct a left either.
   *
   * @param val Left value
   * @param <L> Left value type
   * @param <R> Right value type
   *
   * @return An either wrapping val.
   *
   * @throws NullPointerException if val is null.
   */
  public static < L, R > Either < L, R > left(L val) {
    return new Either<>(Objects.requireNonNull(val), null);
  }

  /**
   * Construct a right either.
   *
   * @param val Right value
   * @param <L> Left value type
   * @param <R> Right value type
   *
   * @return An either wrapping val.
   *
   * @throws NullPointerException if val is null.
   */
  public static < L, R > Either < L, R > right(R val) {
    return new Either<>(null, Objects.requireNonNull(val));
  }
}
