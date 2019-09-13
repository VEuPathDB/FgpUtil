package org.gusdb.fgputil.collection;

import java.util.Map;

public interface WriteableMap<K,V> extends ReadOnlyMap<K,V>, Map<K,V> {

  public abstract Map<K,V> getUnderlyingMap();

  @Override
  public default boolean containsKey(Object key) {
    return getUnderlyingMap().containsKey(key);
  }

  @Override
  public default boolean containsValue(Object value) {
    return getUnderlyingMap().containsValue(value);
  }

  @Override
  public default V get(Object key) {
    return getUnderlyingMap().get(key);
  }

  @Override
  public default V put(K key, V value) {
    return getUnderlyingMap().put(key, value);
  }

  @Override
  public default V remove(Object key) {
    return getUnderlyingMap().remove(key);
  }

  @Override
  public default void putAll(Map<? extends K, ? extends V> map) {
    getUnderlyingMap().putAll(map);
  }

  @Override
  public default void clear() {
    getUnderlyingMap().clear();
  }

}
