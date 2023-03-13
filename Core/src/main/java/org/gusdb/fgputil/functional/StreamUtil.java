package org.gusdb.fgputil.functional;

import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * A collection of utilities to provide extra functionality and convenience when
 * working with Java 8 Streams.
 */
public class StreamUtil {

  /**
   * Returns a function that converts any T to an {@link Entry<Integer,T>} where the key of each
   * entry is an integer that increments each time the function is called.  Passing it
   * to the Stream.map() method causes this key to effectively act as an index of the
   * object i.e. the 0-based order that the objects are processed.
   *
   * Note for parallel streams, processing order is not necessarily the same as the
   * original stream order, even for ordered streams.  Thus this index may not be correct
   * in those cases.  It is a vastly simplified algorithm compared to more comprehensive
   * implementations such as Guava's Streams.mapWithIndex().
   *
   * @param <T> type of the stream being processed
   * @return function that generates indexed values
   */
  public static <T> Function<T, Entry<Integer,T>> toIndexedEntry() {
    AtomicInteger indexWrapper = new AtomicInteger(0);
    return obj -> {
      final int index = indexWrapper.getAndIncrement();
      return new Entry<Integer,T>() {
        @Override
        public Integer getKey() {
          return index;
        }
        @Override public T getValue() {
          return obj;
        }
        @Override
        public T setValue(Object value) {
          throw new UnsupportedOperationException();
        }
      };
    };
  }
}
