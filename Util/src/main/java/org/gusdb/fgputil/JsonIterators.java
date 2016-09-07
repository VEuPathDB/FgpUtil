package org.gusdb.fgputil;

import java.util.Iterator;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;

public class JsonIterators {

  private JsonIterators() {}

  public static Iterable<JsonType> arrayIterable(JSONArray array) {
    return new JsonArrayIterable(array);
  }

  public static Iterable<Entry<String, JsonType>> objectIterable(JSONObject obj) {
    return new JsonObjectIterable(obj);
  }

  private static class JsonArrayIterable implements Iterable<JsonType> {

    private final JSONArray _array;
    private int _currentIndex = 0;
    private boolean _iteratorCalled = false;

    public JsonArrayIterable(JSONArray array) {
      _array = array;
    }

    @Override
    public Iterator<JsonType> iterator() {
      if (_iteratorCalled) {
        throw new IllegalStateException("Only one call to iterator() per instance allowed.");
      }
      _iteratorCalled = true;
      return new Iterator<JsonType>() {
        @Override
        public boolean hasNext() {
          return _currentIndex < _array.length();
        }
        @Override
        public JsonType next() {
          return new JsonType(_array.get(_currentIndex++));
        }
        @Override
        public void remove() {
          throw new UnsupportedOperationException(
              "Cannot remove item from JSONArray from iterator.");
        }
      };
    }
  }

  private static class JsonObjectIterable implements Iterable<Entry<String, JsonType>> {

    private final JSONObject _object;
    private Iterator<String> _iterator;
    private boolean _iteratorCalled = false;

    public JsonObjectIterable(JSONObject object) {
      _object = object;
    }

    @Override
    public Iterator<Entry<String, JsonType>> iterator() {
      if (_iteratorCalled) {
        throw new IllegalStateException("Only one call to iterator() per instance allowed.");
      }
      _iteratorCalled = true;
      _iterator = _object.keySet().iterator();
      return new Iterator<Entry<String, JsonType>>() {
        @Override
        public boolean hasNext() {
          return _iterator.hasNext();
        }
        @Override
        public Entry<String, JsonType> next() {
          final String nextKey = _iterator.next();
          return new Entry<String, JsonType>() {
            @Override
            public String getKey() {
              return nextKey;
            }
            @Override
            public JsonType getValue() {
              return new JsonType(_object.get(nextKey));
            }
            @Override
            public JsonType setValue(JsonType value) {
              // unsure of the desired API here so disallowing for now
              throw new UnsupportedOperationException(
                  "Cannot change value in JSONObject from iterator.");
            }
          };
        }
        @Override
        public void remove() {
          throw new UnsupportedOperationException(
              "Cannot remove item from JSONArray from iterator.");
        }
      };
    }
  }
}
