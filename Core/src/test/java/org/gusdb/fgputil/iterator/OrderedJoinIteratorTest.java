package org.gusdb.fgputil.iterator;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.junit.Assert;
import org.junit.Test;

public class OrderedJoinIteratorTest {

  private static List<Integer> LEFT = List.of(
      1, 2, 2, 4, 6, 6, 6, 7, 8, 8
  );

  private static List<String> RIGHT = List.of(
      "0", "1", "2", "3", "5", "7", "8", "9"
  );

  private static List<TwoTuple<Long, Integer>> EXPECTED = List.of(
      new TwoTuple<>(0L, 1),
      new TwoTuple<>(1L, 2),
      new TwoTuple<>(2L, 3),
      new TwoTuple<>(3L, 1),
      new TwoTuple<>(4L, 1),
      new TwoTuple<>(5L, 1),
      new TwoTuple<>(6L, 3),
      new TwoTuple<>(7L, 2),
      new TwoTuple<>(8L, 3),
      new TwoTuple<>(9L, 1)
  );

  @Test
  public void testIter() {

    // declare the joining iterator
    Iterator<TwoTuple<Long,Integer>> iter = new OrderedJoinIterator<>(
        // iterators
        LEFT.iterator(), RIGHT.iterator(),
        // ID producers
        i -> i.longValue(), s -> Long.valueOf(s),
        (id, lefts, rights) -> new TwoTuple<Long,Integer>(id, lefts.size() + rights.size())
    );

    // collect results of the joining
    List<TwoTuple<Long, Integer>> results = IteratorUtil.toStream(iter).collect(Collectors.toList());

    // confirm they equal expected values
    for (int i = 0; i < EXPECTED.size(); i++) {
      Assert.assertEquals(EXPECTED.get(i), results.get(i));
    }
  }
}
