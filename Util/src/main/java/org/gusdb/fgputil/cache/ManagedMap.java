package org.gusdb.fgputil.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents an InMemoryCache whose key/value pairs must be explicitly added.  Implements the Map
 * interface for familiarity but iteration through entries is not supported.  Entries in the ManagedMap
 * are purged using trimming characteristics passed to constructor, or default if none are passed.
 * 
 * @author rdoherty
 *
 * @param <S> type of key
 * @param <T> type of value
 */
public class ManagedMap<S,T> extends InMemoryCache<S,T> implements Map<S,T> {

  // factory used to insert a new item into the cache
  private ValueFactory<S,T> getValueFactory(T value){
    return key -> value;
  }

  // factory used to create items not found; if item is not found, we throw exception
  private ValueFactory<S,T> getNotFoundFactory() {
    return key -> {
      throw new ValueProductionException("Key not found in managed map.");
    };
  }

  /**
   * Creates a managed map with default settings.
   */
  public ManagedMap() {
    super();
  }

  /**
   * Creates a managed map with custom capacity and trimming characteristics.
   * 
   * @param capacity maximum number of objects that can be cached
   * @param numToTrimOnCapacity number of objects to trim when capacity reached
   */
  public ManagedMap(int capacity, int numToTrimOnCapacity) {
    super(capacity, numToTrimOnCapacity);
  }

  /**
   * @return number of entries currently stored in the map
   */
  @Override
  public int size() {
    return getSize();
  }

  /**
   * @return true if empty, else false
   */
  @Override
  public boolean isEmpty() {
    return getSize() == 0;
  }

  /**
   * @param possible key to value in the map
   * @return true if the passed key represents a value in the map, else false
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean containsKey(Object key) {
    try {
      getValue((S)key, getNotFoundFactory());
      return true;
    }
    catch (ValueProductionException e) {
      return false;
    }
  }

  /**
   * Returns the value associated with the passed key
   * 
   * @param possible key to value in the map
   * @return the value associated with the passed key, or null if that key does not exist
   */
  @SuppressWarnings("unchecked")
  @Override
  public T get(Object key) {
    synchronized(this) {
      try {
        return getValue((S)key, getNotFoundFactory());
      }
      catch (ValueProductionException e) {
        return null;
      }
    }
  }

  /**
   * Puts a new entry into the managed map
   * 
   * @param key new key
   * @param value new item
   * @return previous value associated with the passed key if one exists, else null
   */
  @Override
  public T put(S key, T value) {
    synchronized(this) {
      try {
        T oldValue = get(key);
        if (oldValue != null) {
          remove(key);
        }
        getValue(key, getValueFactory(value));
        return value;
      }
      catch (ValueProductionException e) {
        // this should never happen
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Removes the entry associated with the passed key
   * 
   * @param possible key to item in the map
   * @return previous value associated with the passed key if one exists, else null
   */
  @SuppressWarnings("unchecked")
  @Override
  public T remove(Object key) {
    synchronized(this) {
      T oldValue = get(key);
      expireEntries((S)key);
      return oldValue;
    }
  }

  /**
   * Puts all entries in the passed map into this one
   * 
   * @param map of values
   */
  @Override
  public void putAll(Map<? extends S, ? extends T> map) {
    for (Entry<? extends S, ? extends T> entry : map.entrySet()) {
      put(entry.getKey(), entry.getValue());
    }
  }

  /**
   * Unsupported due to InMemoryCache limitations
   */
  @Override
  public void clear() {
    // TODO: implement - no reason this can't be done relatively easily
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported due to InMemoryCache limitations
   */
  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported due to InMemoryCache limitations
   */
  @Override
  public Set<S> keySet() {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported due to InMemoryCache limitations
   */
  @Override
  public Collection<T> values() {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported due to InMemoryCache limitations
   */
  @Override
  public Set<java.util.Map.Entry<S, T>> entrySet() {
    throw new UnsupportedOperationException();
  }

}
