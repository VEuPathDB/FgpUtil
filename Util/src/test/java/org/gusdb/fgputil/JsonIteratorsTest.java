package org.gusdb.fgputil;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class JsonIteratorsTest {

  @Test
  public void testArrayIter() throws Exception {
    JSONArray array = new JSONArray("[]");
    testCount(array, 0);
    array = new JSONArray("[ 2, null, true, 'blah' ]");
    testCount(array, 4);
  }

  private void testCount(JSONArray array, int expected) {
    int count = 0;
    List<String> vals = new ArrayList<>();
    for (JsonType value : JsonIterators.arrayIterable(array)) {
      count++;
      vals.add(value.toString());
    }
    System.out.println(FormatUtil.arrayToString(vals.toArray()));
    assertEquals(expected, count);
  }

  @Test
  public void testObjectIter() throws Exception {
    JSONObject obj = new JSONObject("{}");
    testCount(obj, 0);
    obj = new JSONObject("{ 1: b, blah: null, a: true, 4: 6.78E34, true: false, null: [], []: {}, {}: [] }");
    testCount(obj, 8);
  }

  private void testCount(JSONObject obj, int expected) {
    int count = 0;
    List<String> keys = new ArrayList<>();
    List<String> vals = new ArrayList<>();
    List<String> types = new ArrayList<>();
    for (Entry<String, JsonType> entry : JsonIterators.objectIterable(obj)) {
      count++;
      keys.add(entry.getKey());
      vals.add(entry.getValue().toString());
      types.add(entry.getValue().getType().name());
    }
    System.out.println(FormatUtil.arrayToString(keys.toArray()));
    System.out.println(FormatUtil.arrayToString(vals.toArray()));
    System.out.println(FormatUtil.arrayToString(types.toArray()));
    System.out.println(obj.toString(2));
    assertEquals(expected, count);
  }
}
