package org.gusdb.fgputil.collection;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.Tuples.TwoTuple;

public class FixedSizeStringMap implements Map<String,String> {

  public static class Builder {

    private final Map<String,Integer> _keyIndex;

    public Builder(String[] keys) {
      if (keys == null || keys.length == 0)
        throw new IllegalArgumentException("FixedSizeStringMap must have at least one key.");
      _keyIndex = new LinkedHashMap<>();
      for (int i = 0; i < keys.length; i++) {
        if (keys[i] == null)
          throw new IllegalArgumentException("No keys can be null.");
        _keyIndex.put(keys[i], i);
      }
    }

    public Set<String> keySet() {
      return _keyIndex.keySet();
    }

    public int size() {
      return _keyIndex.size();
    }

    public FixedSizeStringMap build() {
      return new FixedSizeStringMap(_keyIndex);
    }
  }

  private final Map<String,Integer> _keyIndex;
  private String[] _values;

  private FixedSizeStringMap(Map<String,Integer> keyIndex) {
    _keyIndex = keyIndex;
  }

  public FixedSizeStringMap putAll(String[] values) {
    if (values.length > _keyIndex.size())
      throw new IllegalArgumentException("Only arrays of size " + _keyIndex.size() + " or smaller are allowed.");
    _values = values;
    return this;
  }

  @Override
  public int size() {
    return _keyIndex.size();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    return _keyIndex.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    if (_values == null) return false;
    for (int i = 0; i < _values.length; i++) {
      if (_values[i] != null && _values[i].equals(value))
        return true;
    }
    return false;
  }

  @Override
  public String get(Object key) {
    if (_values == null) return null;
    Integer index = _keyIndex.get(key);
    return index == null ? null : index >= _values.length ? null : _values[index];
  }

  @Override
  public String put(String key, String value) {
    Integer index = _keyIndex.get(key);
    if (index == null)
      throw new IllegalArgumentException("Only preset keys can be assigned. '" + key + "' was not present.");
    expandToFixedSize(index);
    String oldValue = _values[index];
    _values[index] = value;
    return oldValue;
  }

  private void expandToFixedSize(int requestedIndex) {
    if (_values == null)
      _values = new String[_keyIndex.size()];
    else if (requestedIndex >= _values.length)
      // must have initialized with a smaller array but now wants to put a value at a higher index
      _values = Arrays.copyOf(_values, _keyIndex.size());
  }

  @Override
  public String remove(Object key) {
    return put(key.toString(), null);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> m) {
    for (String key : m.keySet()) {
      put(key, m.get(key));
    }
  }

  @Override
  public void clear() {
    _values = null;
  }

  @Override
  public Set<String> keySet() {
    return _keyIndex.keySet();
  }

  @Override
  public Collection<String> values() {
    expandToFixedSize(_keyIndex.size());
    return Arrays.asList(_values);
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    Set<Entry<String,String>> entries = new HashSet<>();
    for (String key : _keyIndex.keySet()) {
      entries.add(new TwoTuple<String,String>(key, get(key)));
    }
    return entries;
  }

}
