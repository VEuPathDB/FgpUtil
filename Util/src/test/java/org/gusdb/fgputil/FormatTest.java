package org.gusdb.fgputil;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.FormatUtil.Style;
import org.junit.Assert;
import org.junit.Test;

public class FormatTest {

  @Test
  public void testIterableJoin() {
    List<String> list = Arrays.asList(new String[]{ "a", "b", "c" });
    String joined = FormatUtil.join(list, "|");
    Assert.assertEquals("a|b|c", joined);
  }

  @Test
  public void testArrayToString() {
    String[] sample = { "a", "b", "c", "d" };
    System.out.println(FormatUtil.arrayToString(sample));
  }
  
  @Test
  public void testPrettyPrint() {
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
  public void testPercentFormat() {
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
  public void testExceptionToString() {
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
  public void testUnderscoreFormatter() {
    for (String[] testCase : UNDERSCORE_TEST_CASES) {
      Assert.assertEquals(testCase[1], FormatUtil.toUnderscoreFormat(testCase[0]));
    }
  }

  private static String[][] TYPICAL_USE = {
    { "uid", "2452345234" },
    { "dsid", "24352345234" },
    { "file", "someWeirdoFileForGbrowse.bed" }
  };

  @Test
  public void testMapEncoder() {
    testMapEncoder(UNDERSCORE_TEST_CASES);
    testMapEncoder(TYPICAL_USE);
  }

  private static void testMapEncoder(String[][] testCase) {
    Map<String,String> inputMap = new HashMap<>();
    for (String[] pair : testCase) {
      inputMap.put(pair[0], pair[1]);
    }
    String codedValue = EncryptionUtil.encodeMap(inputMap);
    System.out.println(NL + "Encoded Map (" + codedValue.length() + "): " + codedValue);
    Map<String,String> outputMap = EncryptionUtil.decodeEncodedMap(codedValue);
    System.out.println(NL + "Decoded Map: " + FormatUtil.prettyPrint(outputMap, Style.MULTI_LINE));
  }

  @Test
  public void testDateFormatters() {
    Date d0 = new Date();
    String s0 = FormatUtil.formatDate(d0);
    String s1 = FormatUtil.formatDateTime(d0);
    Date d1 = FormatUtil.parseDate(s0);
    //Date d2 = FormatUtil.parseDateTime(s0);
    //Date d3 = FormatUtil.parseDate(s1);
    Date d4 = FormatUtil.parseDateTime(s1);
    String s2 = FormatUtil.formatDate(d1);
    String s3 = FormatUtil.formatDateTime(d1);
    //String s4 = FormatUtil.formatDate(d2);
    //String s5 = FormatUtil.formatDateTime(d2);
    //String s6 = FormatUtil.formatDate(d3);
    //String s7 = FormatUtil.formatDateTime(d3);
    String s8 = FormatUtil.formatDate(d4);
    String s9 = FormatUtil.formatDateTime(d4);
    System.out.println("s0: " + s0);
    System.out.println("s1: " + s1);
    System.out.println("s2: " + s2);
    System.out.println("s3: " + s3);
    //System.out.println("s4: " + s4);
    //System.out.println("s5: " + s5);
    //System.out.println("s6: " + s6);
    //System.out.println("s7: " + s7);
    System.out.println("s8: " + s8);
    System.out.println("s9: " + s9);
  }
}
