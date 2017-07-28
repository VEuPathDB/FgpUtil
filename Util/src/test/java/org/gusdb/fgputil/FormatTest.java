package org.gusdb.fgputil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil.Style;
import org.junit.Assert;
import org.junit.Test;

public class FormatTest {

  @Test
  public void testIterableJoin() throws Exception {
    List<String> list = Arrays.asList(new String[]{ "a", "b", "c" });
    String joined = FormatUtil.join(list, "|");
    Assert.assertEquals("a|b|c", joined);
  }

  @Test
  public void testArrayToString() throws Exception {
    String[] sample = { "a", "b", "c", "d" };
    System.out.println(FormatUtil.arrayToString(sample));
  }
  
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

  private static String[][] UNDERSCORE_TEST_CASES = {
    { "name", "name" },
    { "displayName", "display_name" },
    { "whoIsInThere", "who_is_in_there" },
    { "myDSLLanguage", "my_dsl_language" },
    { "XMLGoodies", "xml_goodies" },
    { "WhatEverMan", "what_ever_man" },
    { "big_boy", "big_boy" },
    { "Under_Score_mE", "under_score_m_e" },
    { "blah_bl_AH", "blah_bl_ah" }
  };

  @Test
  public void testUnderscoreFormatter() throws Exception {
    for (String[] testCase : UNDERSCORE_TEST_CASES) {
      Assert.assertEquals(testCase[1], FormatUtil.toUnderscoreFormat(testCase[0]));
    }
  }
}
