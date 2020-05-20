package org.gusdb.fgputil;

import static org.junit.Assert.assertEquals;

import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.junit.Test;

public class TuplesTest {

  @Test
  public void testTwoTuple() {
    TwoTuple<Integer, Float> result = getPlus5(3, 4.5F);
    assertEquals((Integer)8, result.getFirst());
    assertEquals((Float)9.5F, result.getSecond());
  }
  
  private TwoTuple<Integer, Float> getPlus5(int integer, float floater) {
    return new TwoTuple<Integer, Float>(integer + 5, floater + 5);
  }
  
  @Test
  public void testThreeTuple() {
    ThreeTuple<Integer, Float, String> result = getPlus5(3, 4.5F, "blah");
    assertEquals((Integer)8, result.getFirst());
    assertEquals((Float)9.5F, result.getSecond());
    assertEquals("blah5", result.getThird());
  }
  
  private ThreeTuple<Integer, Float, String> getPlus5(int integer, float floater, String stringer) {
    return new ThreeTuple<Integer, Float, String>(integer + 5, floater + 5, stringer + 5);
  }
}
