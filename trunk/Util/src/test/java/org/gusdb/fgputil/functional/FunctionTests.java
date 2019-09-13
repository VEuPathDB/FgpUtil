package org.gusdb.fgputil.functional;

import static java.util.Arrays.asList;
import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.findFirstIndex;
import static org.gusdb.fgputil.functional.Functions.mapToListWithIndex;
import static org.gusdb.fgputil.functional.Functions.reduce;
import static org.gusdb.fgputil.functional.Functions.reduceWithIndex;
import static org.gusdb.fgputil.functional.Functions.zipToList;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces.BinaryFunction;
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

  @Test
  public void zipperTest() {
    Integer[][] tests = {
        { 1, 2, 3 },
        { 1, 2, 3, 4, 5, 6 },
        { 1, 2, 3, 4, 5, 6, 7, 8, 9 }
    };
    BinaryFunction<Character, Integer, String> zipper1 = (c, i) -> "" + c + i;
    BinaryFunction<Integer, Character, String> zipper2 = (i, c) -> "" + c + i;
    List<List<String>> results = new ArrayList<>(4);
    for (Integer[] test : tests) {
      results.add(zipToList(CHARS, asList(test), zipper1, true));
      results.add(zipToList(CHARS, asList(test), zipper1, false));
      results.add(zipToList(asList(test), CHARS, zipper2, true));
      results.add(zipToList(asList(test), CHARS, zipper2, false));
      for (List<String> result : results) {
        System.out.println(FormatUtil.join(result, ","));
      }
      results.clear();
    }
  }

  @Test
  public void findFirstIndexTest() {
    assertEquals(3, findFirstIndex(CHARS, c -> c == 'c'));
    assertEquals(-1, findFirstIndex(CHARS, c -> c == 'z'));
    assertEquals(0, findFirstIndex(CHARS, c -> c > 'b'));
  }
}
