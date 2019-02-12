package org.gusdb.fgputil.json;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule;
import org.gusdb.fgputil.functional.Result;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

public class JsonUtil {

  // Singular configurable instance of jackson's object <-> json mapper.
  public static final ObjectMapper Jackson = new ObjectMapper()
      .registerModule(new JsonOrgModule());

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

  public static JSONArray getJsonArrayOrDefault(JSONObject obj, String key, JSONArray defaultValue) {
    return (obj.has(key) ? obj.getJSONArray(key) : defaultValue);
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
    for (String key : JsonUtil.getKeys(json)) {
      map.put(key, json.getString(key));
    }
    return map;
  }

  public static String[] toStringArray(JSONArray json) {
    String[] result = new String[json.length()];

    for (int i = 0; i < result.length; i++) {
      result[i] = json.getString(i);
    }

    return result;
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
   * Gets keys of a JSONObject in a null-safe way.
   *
   * @param obj object from which to retrieve keys
   * @return array of keys or empty array if none exist
   */
  public static Set<String> getKeys(JSONObject obj) {
    String[] keys = JSONObject.getNames(obj);
    return (keys == null ? Collections.emptySet() :
      new HashSet<String>(Arrays.asList(keys)));
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

  /**
   * Convenience method to insure that nulls intended for JSONObject values are
   * converted to JSONObject.NULL
   * @param obj - value to convert
   * @return - obj or JSONObject.NULL
   */
  public static Object convertNulls(Object obj) {
    return obj == null ? JSONObject.NULL : obj;
  }

  /**
   * Creates a deep clone of the passed JSON object and returns it.  Currently this implementation is rather
   * expensive since it serializes the object and then parses it again.  TODO: make more efficient
   *
   * @param json object to clone
   * @return clone
   */
  public static JSONObject clone(JSONObject json) {
    return new JSONObject(json.toString());
  }

  /**
   * Creates a deep clone of the passed JSON array and returns it.  Currently this implementation is rather
   * expensive since it serializes the array and then parses it again.  TODO: make more efficient
   *
   * @param json arrat to clone
   * @return clone
   */
  public static JSONArray clone(JSONArray json) {
    return new JSONArray(json.toString());
  }

  /**
   * Attempt to deserialize the given {@link JSONObject} into the given class
   * type.
   *
   * @param json JSONObject source
   * @param cls  Target class
   * @param <T>  Target class type
   *
   * @return A result containing either the constructed object resulting from
   *         the deserialization, or if the object could not be constructed, the
   *         Exception thrown while attempting the Deserialization.
   */
  public static <T> Result<Exception, T> jsonToPojo(JSONObject json, Class<T> cls) {
    try {
      return Result.value(Jackson.convertValue(json, cls));
    } catch (Exception e) {
      return Result.error(e);
    }
  }

  /**
   * Attempt to represent an arbitrary object as a JSON string.
   *
   * @param any Object to serialize
   *
   * @return A result containing either the JSON representation of the given
   *         object, or if the object could not be serialized, the Exception
   *         that was thrown when attempting serialization.
   */
  public static Result<Exception, String> toJsonString(Object any) {
    try {
      return Result.value(Jackson.writeValueAsString(any));
    } catch (Exception e) {
      return Result.error(e);
    }
  }

  /**
   * Attempt to convert the given object into a {@link JSONObject}.
   *
   * @param any Object to convert to JSON
   *
   * @return A Result containing either the JSONObject representation of the
   *         given object, or if the object could not be convrted, the Exception
   *         that was thrown when attempting the conversion.
   */
  public static Result<Exception, JSONObject> toJSONObject(Object any) {
    try {
      return Result.value(Jackson.convertValue(any, JSONObject.class));
    } catch (Exception e) {
      return Result.error(e);
    }
  }
}
