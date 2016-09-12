package org.gusdb.fgputil.functional;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.gusdb.fgputil.ListBuilder;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class IterablesTest {

  List<Collection<Integer>> TEST_CASE_1 = new ListBuilder<Collection<Integer>>()
      .add(Collections.EMPTY_LIST)
      .add(Arrays.asList(new Integer[]{ 0,1,2,3,4,5 }))
      .add(Collections.EMPTY_LIST)
      .add(Arrays.asList(new Integer[]{ 6,7,8 }))
      .add(Arrays.asList(new Integer[]{ 9 }))
      .add(Arrays.asList(new Integer[]{ }))
      .add(Arrays.asList(new Integer[]{ 10,11 }))
      .add(Arrays.asList(new Integer[]{ 12 }))
      .add(Collections.EMPTY_LIST)
      .toList();

  Integer[] CASE_1_DESIRED = new Integer[]{ 0,1,2,3,4,5,6,7,8,9,10,11,12 };

  @Test
  public void test() throws Exception {
    Iterator<Integer> result = Functions.flatten(TEST_CASE_1.iterator());
    int index = 0;
    while (result.hasNext()) {
      int nextItem = result.next();
      System.out.println("Next item: " + nextItem);
      assertEquals((int)CASE_1_DESIRED[index], nextItem);
      index++;
    }
    assertEquals(CASE_1_DESIRED.length, index);
  }
}
