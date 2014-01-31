package org.gusdb.fgputil;

import java.util.HashMap;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil.Style;
import org.junit.Test;

public class FormatTest {

  @Test
  public void testPrettyPrint() throws Exception {
    String NL = FormatUtil.NL;
    Map<Integer,String> emptyMap = new HashMap<>();
    Map<Integer,String> fullMap = new MapBuilder<>(1, "One").put(2, "Two")
        .put(3, "Three").put(4, "Four").put(5, "Five").toMap();
    System.out.println(new StringBuilder()
        .append(FormatUtil.prettyPrint(emptyMap, Style.SINGLE_LINE)).append(NL)
        .append(FormatUtil.prettyPrint(emptyMap, Style.MULTI_LINE)).append(NL)
        .append(FormatUtil.prettyPrint(fullMap, Style.SINGLE_LINE)).append(NL)
        .append(FormatUtil.prettyPrint(fullMap, Style.MULTI_LINE)).append(NL)
        .toString());
  }
    
  @Test
  public void testPercentFormat() throws Exception {
    Integer[][] cases = new Integer[][]{
        { 12, 100 },
        { 56, 127 },
        { 0, 40 },
        { -8, 20 },
        { 5 , 0 }
    };
    for (Integer[] cas : cases) {
      System.out.println(cas[0] + "/" + cas[1] + " = " +
          FormatUtil.getPctFromRatio(cas[0], cas[1]));
    }
  }
  
  @Test
  public void testExceptionToString() throws Exception {
    Exception orig = new Exception("Nested Exception");
    Exception e = new Exception("Oh no!", orig);
    String stackTrace = FormatUtil.getStackTrace(e);
    System.out.println(stackTrace);
  }
}
