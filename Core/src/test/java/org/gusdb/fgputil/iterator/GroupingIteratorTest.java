package org.gusdb.fgputil.iterator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

public class GroupingIteratorTest {

  @Test
  public void alphabetTest() {

    // build expected results
    Integer[] successSizes = new Integer[] { 3, 2, 2, 1, 2 };

    // build test data
    List<String> words = Arrays.asList(new String[] {
      "aardvark", "ant", "antelope",
      "beaver", "bee",
      "cat", "cheetah",
      "deer",
      "elephant", "earwig"
    });

    // run test
    Iterator<List<String>> groups = new GroupingIterator<String>(
      words.iterator(),
      (w1, w2) -> w1.charAt(0) == w2.charAt(0)
    );

    // check results
    int i = 0;
    for (List<String> group : IteratorUtil.toIterable(groups)) {
      Assert.assertEquals((long)successSizes[i++], group.size());
    }
  }

  @Test
  public void mapTest() {

    // build expected results
    int expectedNumGroups = 10;
    int expectedNumPerGroup = 3;

    // build test data
    final String MATCH_COLUMN = "pk";
    List<Map<String,String>> rows = new ArrayList<>();
    for (int i = 0; i < expectedNumGroups; i++) {
      for (int j = 0; j < expectedNumPerGroup; j++) {
        Map<String,String> row = new HashMap<>();
        row.put(MATCH_COLUMN, "pk" + i);
        row.put("varName", "var" + j);
        row.put("value", "someValue");
        rows.add(row);
      }
    }
    //System.out.println("Initial list has " + rows.size() + " rows:");
    //rows.stream().forEach(row -> System.out.println(FormatUtil.prettyPrint(row, Style.SINGLE_LINE)));

    // run test
    Iterator<List<Map<String,String>>> groups = new GroupingIterator<Map<String,String>>(
      rows.iterator(),
      (r1, r2) -> r1.get(MATCH_COLUMN).equals(r2.get(MATCH_COLUMN))
    );

    // check results
    int count = 0;
    for (List<Map<String,String>> group : IteratorUtil.toIterable(groups)) {
      count++;
      //System.out.println("Size of group " + (count + 1) + ": " + group.size());
      Assert.assertEquals(expectedNumPerGroup, group.size());
      //group.stream().forEach(row -> System.out.println(FormatUtil.prettyPrint(row, Style.SINGLE_LINE)));
    }
    Assert.assertEquals(expectedNumGroups, count);
  }
}
