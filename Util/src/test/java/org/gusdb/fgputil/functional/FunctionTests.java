package org.gusdb.fgputil.functional;

import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.mapToListWithIndex;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.gusdb.fgputil.functional.FunctionalInterfaces.BinaryFunction;
import org.junit.Test;

public class FunctionTests {

  @Test
  public void testMapWithIndexes() {
    List<Character> chars = Arrays.asList(new Character[]{ 'x', 'a', 'b', 'c', 'd', 'e' });
    String result = join(mapToListWithIndex(chars, new BinaryFunction<Character,Integer,String>(){
      @Override public String apply(Character letter, Integer index) { return "" + index + letter; } }).toArray(), ",");
    assertEquals(result, "0x,1a,2b,3c,4d,5e");
  }
}
