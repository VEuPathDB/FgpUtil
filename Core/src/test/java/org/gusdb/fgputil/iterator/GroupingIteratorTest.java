package org.gusdb.fgputil.iterator;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class GroupingIteratorTest {

  @Test
  public void alphabetTest() {
    List<String> words = Arrays.asList(new String[] {
      "aardvark", "ant", "antelope",
      "beaver", "bee",
      "cat", "cheetah",
      "deer",
      "elephant", "earwig"
    });
    Integer[] successSizes = new Integer[] { 3, 2, 2, 1, 2 };
    Iterator<List<String>> groups = new GroupingIterator<String>(
      words.iterator(),
      (w1, w2) -> w1.charAt(0) == w2.charAt(0)
    );
    int i = 0;
    for (List<String> group : IteratorUtil.toIterable(groups)) {
      Assert.assertEquals((long)successSizes[i++], group.size());
    }
  }
}
