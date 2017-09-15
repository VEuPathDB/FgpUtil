package org.gusdb.fgputil.functional;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.mapToListWithIndex;
import static org.gusdb.fgputil.functional.Functions.reduce;
import static org.gusdb.fgputil.functional.Functions.reduceWithIndex;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class FunctionTests {

  private static final List<Character> CHARS = Arrays.asList(new Character[]{ 'x', 'a', 'b', 'c', 'd', 'e' });

  @Test
  public void testMapWithIndexes() {
    String result = join(mapToListWithIndex(CHARS, (letter, index) -> "" + index + letter).toArray(), ",");
    assertEquals(result, "0x,1a,2b,3c,4d,5e");
  }

  @Test
  public void testReduceWithIndexes() {
    String result = reduceWithIndex(CHARS,
        (sb, letter, index) -> sb.append(index == 0 ? "" : ",").append(index).append(letter),
        new StringBuilder()).toString();
    assertEquals(result, "0x,1a,2b,3c,4d,5e");
  }

  @Test
  public void javaStreamReduce() {
    int asciiSum1 = CHARS.stream().reduce(0, (sum, nextChar) -> sum + (int)nextChar, Integer::sum);
    int asciiSum2 = reduce(CHARS, (sum, nextChar) -> sum + (int)nextChar, 0);
    assertEquals(asciiSum1, 615);
    assertEquals(asciiSum2, 615);
  }
}
