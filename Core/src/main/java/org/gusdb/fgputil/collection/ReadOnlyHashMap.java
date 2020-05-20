package org.gusdb.fgputil.collection;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.gusdb.fgputil.MapBuilder;

public class ReadOnlyHashMap<K,V> implements ReadOnlyMap<K,V> {

  public static class Builder<K,V> extends MapBuilder<K,V> {

    public Builder() { }

    public Builder(Map<K,V> initialMap) {
      super(initialMap);
    }

    public ReadOnlyHashMap<K,V> build() {
      return new ReadOnlyHashMap<>(toMap());
    }
  }

  protected Map<K,V> _map;

  public static <K,V> Builder<K,V> builder() {
    return new Builder<>();
  }

  public ReadOnlyHashMap(Map<K,V> map) {
    _map = (map instanceof LinkedHashMap ? new LinkedHashMap<>(map) : new HashMap<>(map));
  }

  @Override
  public WriteableMap<K,V> toWriteableMap() {
    return new WriteableHashMap<K,V>(_map);
  }

  @Override
  public int size() {
    return _map.size();
  }

  @Override
  public boolean isEmpty() {
    return _map.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return _map.containsKey(key);
  }

  @Override
  public Set<K> keySet() {
    return _map.keySet();
  }

  @Override
  public Collection<V> values() {
    return _map.values();
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    return _map.entrySet();
  }

  @Override
  public V get(Object key) {
    return _map.get(key);
  }
}
