package org.gusdb.fgputil.iterator;

import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.ORDERED;
import static java.util.Spliterator.SORTED;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class GroupingStream {

  public static <T> Stream<List<T>> create(Stream<T> source, BiFunction<T,T,Boolean> isMatch) {
    Iterator<List<T>> groupingIterator = new GroupingIterator<>(
        source.iterator(), isMatch);
    Spliterator<List<T>> spliterator = Spliterators.spliteratorUnknownSize(
        groupingIterator, SORTED | ORDERED | IMMUTABLE);
    return StreamSupport
        .stream(spliterator, false)
        .onClose(source::close);
  }

}
