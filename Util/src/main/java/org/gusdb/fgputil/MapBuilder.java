package org.gusdb.fgputil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Convenience class for building Maps.
 * 
 * @author rdoherty
 *
 * @param <S> key type of the enclosed Map
 * @param <T> value type of the enclosed Map
 */
public class MapBuilder<S,T> {

  public static <S,T> Map<S,T> getMapFromEntries(Collection<Entry<S,T>> entries) {
    return new MapBuilder<S,T>().putAll(entries, entry -> entry).toMap();
  }

  private Map<S,T> _map;

  public MapBuilder() {
    _map = new HashMap<>();
  }

  public MapBuilder(Map<S,T> map) {
    _map = map;
  }

  public MapBuilder(S key, T value) {
    this();
    _map.put(key, value);
  }

  public MapBuilder<S,T> put(S key, T value) {
    _map.put(key, value);
    return this;
  }

  public MapBuilder<S,T> put(Entry<S, T> entry) {
    return put(entry.getKey(), entry.getValue());
  }

  public <R> MapBuilder<S,T> put(R obj, Function<R, Entry<S,T>> converter) {
    Entry<S,T> entry = converter.apply(obj);
    return put(entry.getKey(), entry.getValue());
  }

  public MapBuilder<S,T> putIf(boolean put, S key, T value) {
    return (put ? put(key, value) : this);
  }

  public <R> MapBuilder<S,T> putIf(boolean put, R obj, Function<R, Entry<S,T>> converter) {
    return (put ? put(obj, converter) : this);
  }

  public MapBuilder<S,T> putIf(boolean put, S key, Supplier<T> valueFactory) {
    return (put ? put(key, valueFactory.get()) : this);
  }

  public MapBuilder<S,T> putAll(Map<S,T> map) {
    _map.putAll(map);
    return this;
  }

  public <R> MapBuilder<S,T> putAll(Collection<R> objects, Function<R, Entry<S,T>> converter) {
    for (R obj: objects) {
      Entry<S,T> entry = converter.apply(obj);
      _map.put(entry.getKey(), entry.getValue());
    }
    return this;
  }

  public boolean containsKey(S key) {
    return _map.containsKey(key);
  }

  public T get(S key) {
    return _map.get(key);
  }

  public Map<S,T> toMap() {
    return _map;
  }

}
