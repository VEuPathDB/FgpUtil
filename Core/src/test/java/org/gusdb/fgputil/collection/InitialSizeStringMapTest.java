package org.gusdb.fgputil.collection;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.junit.Test;

/**
 * Unit tests for the InitialSizeStringMap
 */
public class InitialSizeStringMapTest {

  private static final String[] INITIAL_KEYS = new String[] { "col1", "col2", "col3" };

  @Test(expected = IllegalArgumentException.class)
  public void testNoInitialKeys() {
    @SuppressWarnings("unused")
    InitialSizeStringMap.Builder b = new InitialSizeStringMap.Builder(new String[0]);
  }

  @Test
  public void testInsertInitialValues() {
    InitialSizeStringMap map = new InitialSizeStringMap.Builder(INITIAL_KEYS).build();
    map.putAll(new String[] { "a", null, "c" });
    checkInitialContents(map);
    doInsertTests(map, 3);
    checkKeys(map);
  }

  @Test
  public void testInsertPartialInitialValues() {
    InitialSizeStringMap map = new InitialSizeStringMap.Builder(INITIAL_KEYS).build();
    map.putAll(new String[] { "a", null });
    map.put("col3", "c");
    checkInitialContents(map);
    doInsertTests(map, 3);
    checkKeys(map);
  }

  @Test
  public void testInsertSpecificInitialValues() {
    Map<String,String> map = new InitialSizeStringMap.Builder(INITIAL_KEYS).build();
    map.put("col1", "a");
    map.put("col3", "c");
    checkInitialContents(map);
    doInsertTests(map, 3);
    checkKeys(map);
  }

  private void checkInitialContents(Map<String,String> map) {
    checkKeys(map);
    assertArrayEquals(new Object[]{ "a", null, "c" }, map.values().toArray());
    assertArrayEquals(new Object[] {
        new TwoTuple<String,String>("col1", "a"),
        new TwoTuple<String,String>("col2", null),
        new TwoTuple<String,String>("col3", "c")
    }, map.entrySet().toArray());
  }

  private void checkKeys(Map<String,String> map) {
    assertArrayEquals(new Object[]{ "col1", "col2", "col3" }, map.keySet().toArray());
  }

  private static void doInsertTests(Map<String,String> map, int expectedSize) {
    assertEquals(map.get("col1"), "a");
    assertEquals(map.get("col2"), null);
    assertEquals(map.get("col3"), "c");
    assertEquals(map.get("badkey"), null);
    assertEquals(map.containsKey("badkey"), false);
    assertEquals(map.containsKey("col1"), true);
    assertEquals(map.containsKey("col2"), true);
    assertEquals(map.containsValue("a"), true);
    assertEquals(map.containsValue(null), true);
    assertEquals(map.containsValue("badvalue"), false);

    // try some operations
    map.remove("col1");
    assertEquals(map.get("col1"), null);

    // should be the same size because col1 is an initial key (still present!)
    assertEquals(expectedSize, map.size());
  }

  @Test
  public void testInsertSupplementalValues() {
    InitialSizeStringMap map = new InitialSizeStringMap.Builder(INITIAL_KEYS).build();
    map.putAll(new String[] { "a", null, "c" });
    map.put("col4", "d");
    map.put("col5", null);
    doInsertTests(map, 5);
    assertEquals(map.get("col4"), "d");
    assertEquals(map.containsKey("col4"), true);
    assertEquals(map.containsKey("col5"), true);
    assertEquals(map.containsValue("d"), true);
    assertArrayEquals(new Object[]{ null, null, "c", "d", null }, map.values().toArray());
    map.remove("col4");
    assertEquals(map.get("col4"), null);
    assertEquals(map.containsKey("col4"), false);
    assertEquals(map.containsValue("d"), false);
    assertEquals(4, map.size());
    assertArrayEquals(new Object[]{ "col1", "col2", "col3", "col5" }, map.keySet().toArray());
    assertArrayEquals(new Object[]{ null, null, "c", null }, map.values().toArray());
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMultipleSameInitialKeys() {
    String[] initialKeys = new String[] { "a", "b", "c", "b" };
    new InitialSizeStringMap.Builder(initialKeys).build();
  }
}
