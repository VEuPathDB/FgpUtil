package org.gusdb.fgputil.json;

import java.util.function.Function;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Timer;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

import org.junit.Assert;

public class JsonUtilTest {

  @Test
  public void testJsonStuff() throws Exception {
    JSONObject json = new JSONObject();
    Function<String,String> f = str -> str.toLowerCase();
    json.put("f", f);
    System.out.println(json);
  }

  @Test
  public void testSerialization() throws Exception {
    JSONObject json = new JSONObject();
    json.put("bool", true);
    json.put("str1", "blah");
    json.put("int", 5);
    json.put("float", 47238.234);
    json.put("null", JSONObject.NULL);
    json.put("object", new JSONObject().put("key", new JSONArray().put(1).put(2).put(3)));
    json.put("array", new JSONArray().put("blah2").put(true).put(new JSONObject().put("key", true)));
    System.out.println("JSON Structure:" + FormatUtil.NL + json.toString(2));
    String toStringStr = null, serializedStr = null;
    Timer t = new Timer();
    for (int i = 0; i < 10000; i++)
      toStringStr = json.toString();
    long check1= t.getElapsedAndRestart();
    for (int i = 0; i < 10000; i++)
      serializedStr = JsonUtil.serialize(json);
    long check2 = t.getElapsed();
    System.out.println(check1 + " " + toStringStr);
    System.out.println(check2 + " " + serializedStr);
    Assert.assertEquals(toStringStr.length(), serializedStr.length());
  }
}
