package org.gusdb.fgputil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.gusdb.fgputil.Tuples.ThreeTuple;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.junit.jupiter.api.Test;

public class TuplesTest {

  @Test
  public void testTwoTuple() {
    TwoTuple<Integer, Float> result = getPlus5(3, 4.5F);
    assertEquals((Integer)8, result.getFirst());
    assertEquals((Float)9.5F, result.getSecond());
  }

  @SuppressWarnings("SameParameterValue")
  private TwoTuple<Integer, Float> getPlus5(int integer, float floater) {
    return new TwoTuple<>(integer + 5, floater + 5);
  }

  @Test
  public void testThreeTuple() {
    ThreeTuple<Integer, Float, String> result = getPlus5(3, 4.5F, "blah");
    assertEquals((Integer)8, result.getFirst());
    assertEquals((Float)9.5F, result.getSecond());
    assertEquals("blah5", result.getThird());
  }

  @SuppressWarnings("SameParameterValue")
  private ThreeTuple<Integer, Float, String> getPlus5(int integer, float floater, String stringer) {
    return new ThreeTuple<>(integer + 5, floater + 5, stringer + 5);
  }
}
