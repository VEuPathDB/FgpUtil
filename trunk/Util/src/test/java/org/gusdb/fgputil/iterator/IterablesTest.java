package org.gusdb.fgputil.iterator;

import static org.gusdb.fgputil.AlphabetUtils.ALPHABET;
import static org.gusdb.fgputil.AlphabetUtils.NUM_ALPHABET_REPEATS;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.gusdb.fgputil.AlphabetUtils.AlphabetDataProvider;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.ListBuilder;
import org.junit.Test;

public class IterablesTest {

  List<Collection<Integer>> TEST_CASE_1 = new ListBuilder<Collection<Integer>>()
      .add(Collections.emptyList())
      .add(Arrays.asList(new Integer[]{ 0,1,2,3,4,5 }))
      .add(Collections.emptyList())
      .add(Arrays.asList(new Integer[]{ 6,7,8 }))
      .add(Arrays.asList(new Integer[]{ 9 }))
      .add(Arrays.asList(new Integer[]{ }))
      .add(Arrays.asList(new Integer[]{ 10,11 }))
      .add(Arrays.asList(new Integer[]{ 12 }))
      .add(Collections.emptyList())
      .toList();

  Integer[] CASE_1_DESIRED = new Integer[]{ 0,1,2,3,4,5,6,7,8,9,10,11,12 };

  @Test
  public void test() {
    Iterator<Integer> result = IteratorUtil.flatten(TEST_CASE_1.iterator());
    int index = 0;
    while (result.hasNext()) {
      int nextItem = result.next();
      System.out.println("Next item: " + nextItem);
      assertEquals((int)CASE_1_DESIRED[index], nextItem);
      index++;
    }
    assertEquals(CASE_1_DESIRED.length, index);
  }

  @Test
  public void testIteratingStream() throws IOException {
    InputStream in = new IteratingInputStream(new AlphabetDataProvider(NUM_ALPHABET_REPEATS));
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    IoUtil.transferStream(out, in);
    byte[] written = out.toByteArray();
    assertEquals(ALPHABET.length * NUM_ALPHABET_REPEATS, written.length);
  }
}
