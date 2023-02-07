package org.gusdb.fgputil.collection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.fgputil.Tuples.TwoTuple;

/**
 * A map implementation that uses an initial size and set of keys.  Its size
 * will never be smaller than the initial size, but it can be greater if keys
 * are added beyond the initial list.  Use requires first creating a Builder
 * instance that defines the initial keys of the maps it produces
 * 
 * Values can be associated with the initial keys cheaply via an efficient
 * array-based storage of the values, with a map from key to array index is
 * shared across all maps produced by the Builder.
 *
 * Supplemental entries beyond the initial keys can be added, but no more
 * efficiently than a regular HashMap. 
 *
 * Note the produced maps allow null values but not null keys.  The key order
 * during iteration is maintained from the initial keys String[] and then
 * via insertion order for supplemental entries.
 *
 * @author rdoherty
 */
public class InitialSizeStringMap implements Map<String,String> {

  /**
   * Builder class used to create instances of maps that all share the same
   * keys.  Values added to those maps can only be associated with the keys
   * passed to the Builder's constructor.
   */
  public static class Builder {

    private final Map<String,Integer> _keyIndex;

    public Builder(String[] keys) {
      if (keys == null || keys.length == 0)
        throw new IllegalArgumentException("FixedSizeStringMap must have at least one key.");
      _keyIndex = new LinkedHashMap<>(keys.length);
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

    public InitialSizeStringMap build() {
      return new InitialSizeStringMap(_keyIndex);
    }
  }

  private final Map<String,Integer> _keyIndex;

  // _values can be:
  //   1. null (no assigned values)
  //   2. an array with length smaller than size of _keyIndex (assigned with putAll(String[]))
  //   3. an array with length = size of _keyIndex (values for all keys
  private String[] _values;

  private Map<String,String> _supplementalEntries;

  private InitialSizeStringMap(Map<String,Integer> keyIndex) {
    _keyIndex = keyIndex;
  }

  public InitialSizeStringMap putAll(String[] values) {
    if (values.length > _keyIndex.size())
      throw new IllegalArgumentException("Only arrays of size " + _keyIndex.size() + " or smaller are allowed.");
    _values = values;
    return this;
  }

  @Override
  public int size() {
    return _keyIndex.size() + (_supplementalEntries == null ? 0 : _supplementalEntries.size());
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    return _keyIndex.containsKey(key) ||
        (_supplementalEntries != null && _supplementalEntries.containsKey(key));
  }

  @Override
  public boolean containsValue(Object value) {
    // check initial keys' values first
    if (_values != null) {
      for (int i = 0; i < _values.length; i++) {
        if ((value == null && _values[i] == null) ||
            (_values[i] != null && _values[i].equals(value)))
          return true;
      }
    }
    return (_supplementalEntries != null && _supplementalEntries.containsValue(value));
  }

  @Override
  public String get(Object key) {
    if (_values != null) {
      Integer index = _keyIndex.get(key);
      if (index != null) {
        return index >= _values.length ? null : _values[index];
      }
    }
    // key not in initial keys; check supplemental
    return _supplementalEntries == null ? null : _supplementalEntries.get(key);
  }

  @Override
  public String put(String key, String value) {
    Integer index = _keyIndex.get(key);
    if (index != null) {
      expandToFixedSize(index);
      String oldValue = _values[index];
      _values[index] = value;
      return oldValue;
    }
    if (_supplementalEntries == null) {
      _supplementalEntries = new LinkedHashMap<>();
    }
    return _supplementalEntries.put(key, value);
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
    if (_keyIndex.containsKey(key)) {
      return put(key.toString(), null);
    }
    return _supplementalEntries == null ?
        null : _supplementalEntries.remove(key);
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
    if (_supplementalEntries != null)
      _supplementalEntries.clear();
  }

  @Override
  public Set<String> keySet() {
    if (_supplementalEntries == null || _supplementalEntries.isEmpty()) {
      return _keyIndex.keySet();
    }
    Set<String> keys = new LinkedHashSet<>(_keyIndex.keySet());
    keys.addAll(_supplementalEntries.keySet());
    return keys;
  }

  @Override
  public Collection<String> values() {
    expandToFixedSize(_keyIndex.size());
    if (_supplementalEntries == null || _supplementalEntries.isEmpty()) {
      return Arrays.asList(_values);
    }
    List<String> values = new ArrayList<>(Arrays.asList(_values));
    values.addAll(_supplementalEntries.values());
    return values;
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    Set<Entry<String,String>> entries = new LinkedHashSet<>();
    for (String key : _keyIndex.keySet()) {
      entries.add(new TwoTuple<String,String>(key, get(key)));
    }
    if (_supplementalEntries != null) {
      for (String key : _supplementalEntries.keySet()) {
        entries.add(new TwoTuple<String,String>(key, _supplementalEntries.get(key)));
      }
    }
    return entries;
  }

}
