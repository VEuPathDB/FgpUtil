package org.gusdb.fgputil.json;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class JsonType {

  public static enum ValueType {
    OBJECT,
    ARRAY,
    STRING,
    NUMBER,
    BOOLEAN,
    NULL;
  }

  public static enum NumberSubtype {
    LONG,
    DOUBLE,
    NAN;
  }

  private static final String PARSE_ERROR_MSG = "Passed string is not parsable into a JSON value: ";

  private Object _object = null;
  private ValueType _type;
  private NumberSubtype _numberSubtype;

  private JsonType() {}

  public static JsonType parse(String jsonStr) throws JSONException {
    JsonType instance = new JsonType();
    instance._numberSubtype = NumberSubtype.NAN;
    // handle simple cases
    if ("null".equals(jsonStr)) {
      instance._type = ValueType.NULL;
    }
    else if ("true".equals(jsonStr)) {
      instance._type = ValueType.BOOLEAN;
      instance._object = Boolean.TRUE;
    }
    else if ("false".equals(jsonStr)) {
      instance._type = ValueType.BOOLEAN;
      instance._object = Boolean.FALSE;
    }

    // try for object and array, then parse for string and number
    else {
      try {
        // try to convert the passed string to a JSONObject
        instance._object = new JSONObject(jsonStr);
        instance._type = ValueType.OBJECT;
      }
      catch (JSONException e1) {
        // failed to create JSON object, try array
        try {
          instance._object = new JSONArray(jsonStr);
          instance._type = ValueType.ARRAY;
        }
        catch (JSONException e2) {
          // neither object nor array, try number and string by wrapping in brackets
          try {
            JSONArray array = new JSONArray("[ " + jsonStr + " ]");
            instance.setObject(array.get(0));
          }
          catch (JSONException e3) {
            throw new IllegalArgumentException(PARSE_ERROR_MSG + jsonStr);
          }
        }
      }
    }
    return instance;
  }

  public JsonType(Object object) {
    setObject(object);
  }

  private void setObject(Object object) {
    _numberSubtype = NumberSubtype.NAN;
    if (object == null || object.equals(JSONObject.NULL)) {
      _type = ValueType.NULL;
      object = JSONObject.NULL;
    }
    else if (object instanceof Boolean) {
      _type = ValueType.BOOLEAN;
    }
    else if (object instanceof String) {
      _type = ValueType.STRING;
    }
    else if (object instanceof Number) {
      if (object instanceof Integer) {
        _type = ValueType.NUMBER;
        _numberSubtype = NumberSubtype.LONG;
        object = ((Integer)object).longValue();
      }
      else if (object instanceof Long) {
        _type = ValueType.NUMBER;
        _numberSubtype = NumberSubtype.LONG;
      }
      else if (object instanceof Float) {
        _type = ValueType.NUMBER;
        _numberSubtype = NumberSubtype.DOUBLE;
        object = ((Float)object).doubleValue();
      }
      else if (object instanceof Double) {
        _type = ValueType.NUMBER;
        _numberSubtype = NumberSubtype.DOUBLE;
      }
      else {
        throw new UnsupportedOperationException("Only Integer, Long, Float, and Double number types are supported.");
      }
    }
    else if (object instanceof JSONObject) {
      _type = ValueType.OBJECT;
    }
    else if (object instanceof JSONArray) {
      _type = ValueType.ARRAY;
    }
    else {
      throw new IllegalArgumentException(PARSE_ERROR_MSG + object);
    }
    _object = object;
  }

  public ValueType getType() {
    return _type;
  }

  public NumberSubtype getNumberSubtype() {
    return _numberSubtype;
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
    switch (_numberSubtype) {
      case LONG: return ((Long)_object).doubleValue();
      case DOUBLE: // pass through
      default: return (Double)_object;
    }
  }

  public Long getLong() {
    checkType(ValueType.NUMBER, NumberSubtype.LONG, "getLong");
    return (Long)_object;
  }

  public Integer getInteger() {
    checkType(ValueType.NUMBER, NumberSubtype.LONG, "getInteger");
    return ((Long)_object).intValue();
  }

  public Boolean getBoolean() {
    checkType(ValueType.BOOLEAN, "getBoolean");
    return (Boolean)_object;
  }

  private void checkType(ValueType type, NumberSubtype subtype, String call) {
    checkType(type, call);
    if (!subtype.equals(_numberSubtype)) {
      throw new UnsupportedOperationException("Cannot call " +
          call + "() unless subtype is " + subtype.name());
    }
  }

  private void checkType(ValueType type, String call) {
    if (!type.equals(_type)) {
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
