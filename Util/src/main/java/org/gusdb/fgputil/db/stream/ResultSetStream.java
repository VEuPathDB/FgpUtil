package org.gusdb.fgputil.db.stream;

import org.gusdb.fgputil.db.SqlRuntimeException;
import org.gusdb.fgputil.db.stream.ResultSetIterator.RowConverter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import static java.util.Spliterator.*;

public class ResultSetStream<T> implements Stream<T> {

  private final ResultSet rs;
  private final ResultSetIterator<T> it;

  public ResultSetStream(ResultSet rs, RowConverter<T> converter) {
    this.rs = rs;
    this.it = new ResultSetIterator<>(rs, converter);
  }

  @Override
  public Stream<T> filter(Predicate<? super T> predicate) {
    return subStream().filter(predicate);
  }

  @Override
  public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
    return subStream().map(mapper);
  }

  @Override
  public IntStream mapToInt(ToIntFunction<? super T> mapper) {
    return subStream().mapToInt(mapper);
  }

  @Override
  public LongStream mapToLong(ToLongFunction<? super T> mapper) {
    return subStream().mapToLong(mapper);
  }

  @Override
  public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
    return subStream().mapToDouble(mapper);
  }

  @Override
  public <R> Stream<R> flatMap(
    Function<? super T, ? extends Stream<? extends R>> mapper
  ) {
    return subStream().flatMap(mapper);
  }

  @Override
  public IntStream flatMapToInt(
    Function<? super T, ? extends IntStream> mapper
  ) {
    return subStream().flatMapToInt(mapper);
  }

  @Override
  public LongStream flatMapToLong(
    Function<? super T, ? extends LongStream> mapper
  ) {
    return subStream().flatMapToLong(mapper);
  }

  @Override
  public DoubleStream flatMapToDouble(
    Function<? super T, ? extends DoubleStream> mapper
  ) {
    return subStream().flatMapToDouble(mapper);
  }

  @Override
  public Stream<T> distinct() {
    return subStream().distinct();
  }

  @Override
  public Stream<T> sorted() {
    return subStream().sorted();
  }

  @Override
  public Stream<T> sorted(Comparator<? super T> comparator) {
    return subStream().sorted(comparator);
  }

  @Override
  public Stream<T> peek(Consumer<? super T> action) {
    return subStream().peek(action);
  }

  @Override
  public Stream<T> limit(long maxSize) {
    return subStream().limit(maxSize);
  }

  @Override
  public Stream<T> skip(long n) {
    return subStream().skip(n);
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    it.forEachRemaining(action);
  }

  @Override
  public void forEachOrdered(Consumer<? super T> action) {
    it.forEachRemaining(action);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Object[] toArray() {
    return subStream().toArray();
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public <A> A[] toArray(IntFunction<A[]> generator) {
    return subStream().toArray(generator);
  }

  @Override
  public T reduce(T identity, BinaryOperator<T> accumulator) {
    return subStream().reduce(identity, accumulator);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Optional<T> reduce(BinaryOperator<T> accumulator) {
    return subStream().reduce(accumulator);
  }

  @Override
  public <U> U reduce(
    U identity,
    BiFunction<U, ? super T, U> accumulator,
    BinaryOperator<U> combiner
  ) {
    return subStream().reduce(identity, accumulator, combiner);
  }

  @Override
  public <R> R collect(
    Supplier<R> supplier,
    BiConsumer<R, ? super T> accumulator,
    BiConsumer<R, R> combiner
  ) {
    return subStream().collect(supplier, accumulator, combiner);
  }

  @Override
  public <R, A> R collect(Collector<? super T, A, R> collector) {
    return subStream().collect(collector);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Optional<T> min(Comparator<? super T> comparator) {
    return subStream().min(comparator);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Optional<T> max(Comparator<? super T> comparator) {
    return subStream().max(comparator);
  }

  @Override
  public long count() {
    var i = 0L;
    try {
      while (rs.next())
        i++;
    } catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
    return i;
  }

  @Override
  public boolean anyMatch(Predicate<? super T> predicate) {
    return subStream().anyMatch(predicate);
  }

  @Override
  public boolean allMatch(Predicate<? super T> predicate) {
    return subStream().allMatch(predicate);
  }

  @Override
  public boolean noneMatch(Predicate<? super T> predicate) {
    return subStream().noneMatch(predicate);
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Optional<T> findFirst() {
    return subStream().findFirst();
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Optional<T> findAny() {
    return subStream().findAny();
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Iterator<T> iterator() {
    return it;
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Spliterator<T> spliterator() {
    return Spliterators.spliteratorUnknownSize(it, SORTED | ORDERED | IMMUTABLE);
  }

  @Override
  public boolean isParallel() {
    return false;
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Stream<T> sequential() {
    return this;
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Stream<T> parallel() {
    throw new UnsupportedOperationException(
      "Parallel ResultSet streams are not supported");
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Stream<T> unordered() {
    throw new UnsupportedOperationException(
      "Unordered ResultSet streams are not supported");
  }

  @Override
  @SuppressWarnings("NullableProblems")
  public Stream<T> onClose(Runnable closeHandler) {
    return subStream().onClose(closeHandler);
  }

  @Override
  public void close() {
    try {
      rs.close();
    } catch (SQLException e) {
      throw new SqlRuntimeException(e);
    }
  }

  private Stream<T> subStream() {
    return StreamSupport.stream(spliterator(), false)
      .onClose(this::close);
  }
}
