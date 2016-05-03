package org.gusdb.fgputil;

import static org.junit.Assert.assertEquals;
import static java.util.Arrays.asList;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import org.json.JSONObject;
import org.junit.Test;

/**
 * TODO: convert to "real" unit test.  For now just a sanity check on this functionality
 * @author rdoherty
 */
public class ValidationTest {

  private static final String[] requiredProps = { "a", "b", "c" };
  private static final String[] optionalProps = { "d" };

  private static final String[] testSet1 = { "a", "b", "c" };
  private static final String[] testSet2 = { "a", "b", "c", "d" };
  private static final String[] testSet3 = { "a", "c", "d", "e" };

  private static final String[] NONE = { };

  @Test
  public void doTestSet1() {
    runTests(1, testSet1, new Integer[]{ 0, 3, 0, 3, 0, 0, 0, 0 });
  }

  @Test
  public void doTestSet2() {
    runTests(2, testSet2, new Integer[]{ 0, 3, 1, 4, 0, 0, 0, 0 });
  }

  @Test
  public void doTestSet3() {
    runTests(3, testSet3, new Integer[]{ 2, 3, 3, 4, 1, 0, 1, 0 });
  }

  private void runTests(int testSetNum, String[] testSet, Integer[] expectedResults) {
    doTest(testSetNum + "a", testSet, requiredProps, optionalProps, false, expectedResults[0]);
    doTest(testSetNum + "b", testSet, NONE, optionalProps, false, expectedResults[1]);
    doTest(testSetNum + "c", testSet, requiredProps, NONE, false, expectedResults[2]);
    doTest(testSetNum + "d", testSet, NONE, NONE, false, expectedResults[3]);
    doTest(testSetNum + "e", testSet, requiredProps, optionalProps, true, expectedResults[4]);
    doTest(testSetNum + "f", testSet, NONE, optionalProps, true, expectedResults[5]);
    doTest(testSetNum + "g", testSet, requiredProps, NONE, true, expectedResults[6]);
    doTest(testSetNum + "h", testSet, NONE, NONE, true, expectedResults[7]);
  }

  private static void doTest(String testNumber, String[] properties, String[] requiredProps,
      String[] optionalProps, boolean allowExtra, int numExpectedMessages) {
    log("Test " + testNumber + ":");
    List<String> messages = ValidationUtil.validateProperties(
        asList(properties), toSet(requiredProps), toSet(optionalProps), allowExtra);
    log(messages);
    assertEquals(numExpectedMessages, messages.size());
  }

  // just checking for runtime errors when converting JSON
  @Test
  public void testJson() {
    JSONObject json = new JSONObject();
    for (String s : testSet3) {
      json.put(s, true);
    }
    List<String> messages = ValidationUtil.validateProperties(json, toSet(requiredProps), toSet(optionalProps));
    assertEquals(2, messages.size());
  }

  private static Set<String> toSet(String[] arr) {
    return new HashSet<>(Arrays.asList(arr));
  }

  private static void log(String... messages) {
    log(Arrays.asList(messages));
  }

  private static void log(List<String> messages) {
    for (String message: messages) {
      System.out.println(message);
    }
  }
}
