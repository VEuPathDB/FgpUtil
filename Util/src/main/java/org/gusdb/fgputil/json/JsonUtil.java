package org.gusdb.fgputil.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class JsonUtil {

  private JsonUtil() {}

  public static boolean getBooleanOrDefault(JSONObject obj, String key, boolean defaultValue) {
    return (obj.has(key) ? obj.getBoolean(key) : defaultValue);
  }

  public static String getStringOrDefault(JSONObject obj, String key, String defaultValue) {
    return (obj.has(key) ? obj.getString(key) : defaultValue);
  }

  public static int getIntegerOrDefault(JSONObject obj, String key, int defaultValue) {
    return (obj.has(key) ? obj.getInt(key) : defaultValue);
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

  /**
   * Serializes the contents of the JSONObject to a String.  Unlike the
   * toString() method of JSONObject, this function will output JSONObjects in
   * sorted key order so that values can be compared and generate identical
   * hashes and checksums for identical JSON values.
   * 
   * @param json the JSON object
   * @return string representation
   */
  public static String serialize(JSONObject json) {
    StringBuilder sb = new StringBuilder();
    write(json, sb);
    return sb.toString();
  }

  /**
   * Serializes the contents of the JSONArray to a String.  Unlike the
   * toString() method of JSONArray, this function will output JSONObjects in
   * sorted key order so that values can be compared and generate identical
   * hashes and checksums for identical JSON values.
   * 
   * @param json the JSON object
   * @return string representation
   */
  public static String serialize(JSONArray json) {
    StringBuilder sb = new StringBuilder();
    write(json, sb);
    return sb.toString();
  }

  /**
   * Write the contents of the JSONObject as JSON text to a StringBuilder. For
   * compactness, no whitespace is added.  Unlike the toString() method of
   * JSONObject, this function will output objects in sorted key order so
   * that values can be compared and generate identical hashes and checksums
   * for identical JSON values.
   * <p>
   * Warning: This method assumes that the data structure is acyclical.
   *
   * @param jsonObj the JSON object
   * @param out string builder to write to
   * @throws JSONException
   */
  // NOTE: this method was taken and modified from json.org's JSONObject
  private static void write(JSONObject jsonObj, StringBuilder out) throws JSONException {
    boolean commanate = false;
    final int length = jsonObj.length();
    String[] keys = JSONObject.getNames(jsonObj);
    out.append('{');
    if (length == 1) {
      out.append(JSONObject.quote(keys[0]));
      out.append(':');
      writeValue(jsonObj.opt(keys[0]), out);
    }
    else if (length != 0) {
      Arrays.sort(keys);
      for (String key : keys) {
        if (commanate) {
          out.append(',');
        }
        out.append(JSONObject.quote(key));
        out.append(':');
        writeValue(jsonObj.opt(key), out);
        commanate = true;
      }
    }
    out.append('}');
  }

  /**
   * Write the contents of the JSONArray as JSON text to a StringBuilder. For
   * compactness, no whitespace is added.
   * <p>
   * Warning: This method assumes that the data structure is acyclical.
   *
   * @param jsonArr the JSON array
   * @param out string builder to write to
   * @throws JSONException
   */
  // NOTE: this method was taken and modified from json.org's JSONArray
  private static void write(JSONArray jsonArr, StringBuilder out) throws JSONException {
    boolean commanate = false;
    int length = jsonArr.length();
    out.append('[');
    if (length == 1) {
      writeValue(jsonArr.opt(0), out);
    }
    else if (length != 0) {
      for (int i = 0; i < length; i += 1) {
        if (commanate) {
          out.append(',');
        }
        writeValue(jsonArr.opt(i), out);
        commanate = true;
      }
    }
    out.append(']');
  }

  // NOTE: this method was taken and modified from json.org's JSONObject
  private static void writeValue(Object value, StringBuilder out) throws JSONException {
    if (value == null || value.equals(null)) {
      out.append("null");
    }
    else if (value instanceof JSONObject) {
      write((JSONObject) value, out);
    }
    else if (value instanceof JSONArray) {
      write((JSONArray) value, out);
    }
    else if (value instanceof Map) {
      write(new JSONObject((Map<?,?>) value), out);
    }
    else if (value instanceof Collection) {
      write(new JSONArray((Collection<?>) value), out);
    }
    else if (value.getClass().isArray()) {
      write(new JSONArray(value), out);
    }
    else if (value instanceof Number) {
      out.append(JSONObject.numberToString((Number) value));
    }
    else if (value instanceof Boolean) {
      out.append(value.toString());
    }
    else if (value instanceof JSONString) {
      Object o;
      try {
        o = ((JSONString) value).toJSONString();
      }
      catch (Exception e) {
        throw new JSONException(e);
      }
      out.append(o != null ? o.toString() : JSONObject.quote(value.toString()));
    }
    else {
      out.append(JSONObject.quote(value.toString()));
    }
  }

}
