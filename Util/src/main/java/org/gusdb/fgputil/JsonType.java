package org.gusdb.fgputil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonType {

  public static enum ValueType {
    OBJECT, ARRAY, STRING, NUMBER, BOOLEAN, NULL;
  }

  private static final String PARSE_ERROR_MSG = "Passed string is not parsable into a JSON value: ";

  private ValueType _nativeType;
  private Object _object = null;

  public JsonType(String jsonStr) throws JSONException {
    // handle simple cases
    if ("null".equals(jsonStr)) {
      _nativeType = ValueType.NULL;
    }
    else if ("true".equals(jsonStr)) {
      _nativeType = ValueType.BOOLEAN;
      _object = Boolean.TRUE;
    }
    else if ("false".equals(jsonStr)) {
      _nativeType = ValueType.BOOLEAN;
      _object = Boolean.FALSE;
    }

    // try for object and array, then parse for string and number
    else {
      try {
        // try to convert the passed string to a JSONObject
        _object = new JSONObject(jsonStr);
        _nativeType = ValueType.OBJECT;
      }
      catch (JSONException e1) {
        // failed to create JSON object, try array
        try {
          _object = new JSONArray(jsonStr);
          _nativeType = ValueType.ARRAY;
        }
        catch (JSONException e2) {
          // neither object nor array, try number and string by wrapping in brackets
          try {
            JSONArray array = new JSONArray("[ " + jsonStr + " ]");
            setObject(array.get(0));
          }
          catch (JSONException e3) {
            throw new IllegalArgumentException(PARSE_ERROR_MSG + jsonStr);
          }
        }
      }
    }
  }

  public JsonType(Object object) {
    setObject(object);
  }

  private void setObject(Object object) {
    if (object == null || object.equals(JSONObject.NULL)) {
      _nativeType = ValueType.NULL;
      object = JSONObject.NULL;
    }
    else if (object instanceof Boolean) {
      _nativeType = ValueType.BOOLEAN;
    }
    else if (object instanceof String) {
      _nativeType = ValueType.STRING;
    }
    else if (object instanceof Integer) {
      _nativeType = ValueType.NUMBER;
      object = ((Integer)object).doubleValue();
    }
    else if (object instanceof Long) {
      _nativeType = ValueType.NUMBER;
      object = ((Long)object).doubleValue();
    }
    else if (object instanceof Double) {
      _nativeType = ValueType.NUMBER;
    }
    else if (object instanceof JSONObject) {
      _nativeType = ValueType.OBJECT;
    }
    else if (object instanceof JSONArray) {
      _nativeType = ValueType.ARRAY;
    }
    else {
      throw new IllegalArgumentException(PARSE_ERROR_MSG + object);
    }
    _object = object;
  }

  public ValueType getType() {
    return _nativeType;
  }

  public JSONObject getJSONObject() {
    checkType(ValueType.OBJECT, "getJSONObject");
    return (JSONObject)_object;
  }

  public JSONArray getJSONArray() {
    checkType(ValueType.ARRAY, "getJSONArray");
    return (JSONArray)_object;
  }

  public String getString() {
    checkType(ValueType.STRING, "getString");
    return (String)_object;
  }

  public Double getDouble() {
    checkType(ValueType.NUMBER, "getDouble");
    return (Double)_object;
  }

  public Boolean getBoolean() {
    checkType(ValueType.BOOLEAN, "getBoolean");
    return (Boolean)_object;
  }

  private void checkType(ValueType type, String call) {
    if (!type.equals(_nativeType)) {
      throw new UnsupportedOperationException("Cannot call " +
          call + "() unless type is " + type.name());
    }
  }

  @Override
  public String toString() {
    return _object.toString();
  }

  public Object get() {
    return _object;
  }
}
