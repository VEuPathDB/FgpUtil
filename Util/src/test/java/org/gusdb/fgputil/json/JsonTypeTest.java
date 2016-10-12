package org.gusdb.fgputil.json;

import static org.junit.Assert.assertEquals;

import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class JsonTypeTest {

  @Test
  public void nullTest() throws Exception {
    Object value = null;
    JsonType json = new JsonType(value);
    assertEquals(ValueType.NULL, json.getType());
    assertEquals(JSONObject.NULL, json.get());
    value = JSONObject.NULL;
    json = new JsonType(value);
    assertEquals(ValueType.NULL, json.getType());
    assertEquals(JSONObject.NULL, json.get());
  }

  @Test
  public void booleanTest() throws Exception {
    String value = "true";
    JsonType json = JsonType.parse(value);
    assertEquals(ValueType.BOOLEAN, json.getType());
    assertEquals(true, json.getBoolean());
    value = "false";
    json = JsonType.parse(value);
    assertEquals(ValueType.BOOLEAN, json.getType());
    assertEquals(false, json.getBoolean());
  }

  @Test
  public void doubleTest() throws Exception {
    String value = "123.123";
    JsonType json = JsonType.parse(value);
    assertEquals(ValueType.NUMBER, json.getType());
    assertEquals(Double.valueOf(123.123), json.getDouble());
  }

  @Test
  public void stringTest() throws Exception {
    String value = "\"123.123\"";
    JsonType json = JsonType.parse(value);
    assertEquals(ValueType.STRING, json.getType());
    assertEquals("123.123", json.getString());
  }

  @Test
  public void arrayTest() throws Exception {
    String value = "[ \"123.123\", true, null ]";
    JsonType json = JsonType.parse(value);
    assertEquals(ValueType.ARRAY, json.getType());
    assertEquals(3, json.getJSONArray().length());
  }

  @Test
  public void objectTest() throws Exception {
    String value = "{ \"item1\": \"123.123\", \"item2\": true, \"item3\": null }";
    JsonType json = JsonType.parse(value);
    assertEquals(ValueType.OBJECT, json.getType());
    assertEquals(3, json.getJSONObject().length());
  }

  // XXX: FYI this code SHOULD fail in many places, but org.json is NOT a strict parser and
  //   accepts raw strings, both in objects and arrays
  @Test
  public void failureTest() throws Exception {
    String value = "abc";
    JsonType json = JsonType.parse(value);
    assertEquals(ValueType.STRING, json.getType());
    value = "[ abc, def ]";
    json = JsonType.parse(value);
    assertEquals(ValueType.ARRAY, json.getType());
    assertEquals(2, json.getJSONArray().length());
    value = "[ abc def ]";
    json = JsonType.parse(value);
    assertEquals(ValueType.ARRAY, json.getType());
    assertEquals(1, json.getJSONArray().length());
    JSONArray array = new JSONArray(value);
    System.out.println(array);
    JSONObject obj = new JSONObject("{ 1.2: b }");
    System.out.println(obj.getString("1.2"));
  }
}
