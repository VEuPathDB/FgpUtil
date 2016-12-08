package org.gusdb.fgputil.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonUtil {

  private JsonUtil() {}

  public static boolean getBooleanOrDefault(JSONObject obj, String key, boolean defaultValue) {
    return (obj.has(key) ? obj.getBoolean(key) : defaultValue);
  }

  public static String getStringOrDefault(JSONObject obj, String key, String defaultValue) {
    return (obj.has(key) ? obj.getString(key) : defaultValue);
  }

  public static JSONObject getJsonObjectOrDefault(JSONObject obj, String key, JSONObject defaultValue) {
    return (obj.has(key) ? obj.getJSONObject(key) : defaultValue);
  }

  public static JSONArray getOrEmptyArray(JSONArray jsonArrayOrNull) {
    return (jsonArrayOrNull == null ? new JSONArray() : jsonArrayOrNull);
  }

  /**
   * Converts the JSON object to a Map.  Assumes all property values in the passed JSON are
   * Strings; if not, a JSONException will be thrown
   * 
   * @param json JSON object
   * @return map of key/value pairs
   * @throws JSONException thrown in the event of a non-string value
   */
  public static Map<String,String> parseProperties(JSONObject json) throws JSONException {
    Map<String, String> map = new HashMap<>();
    if (json.length() == 0) {
      return map;
    }
    for (String key : JSONObject.getNames(json)) {
      map.put(key, json.getString(key));
    }
    return map;
  }

  public static JSONObject toJsonObject(Map<String,String> map) {
    JSONObject json = new JSONObject();
    for (Entry<String,String> entry : map.entrySet()) {
      json.put(entry.getKey(), entry.getValue());
    }
    return json;
  }

  public static JSONArray toJsonStringArray(String[] strings) {
    JSONArray json = new JSONArray();
    for (String s : strings) {
      json.put(s);
    }
    return json;
  }
}
