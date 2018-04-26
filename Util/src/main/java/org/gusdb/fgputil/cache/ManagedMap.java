package org.gusdb.fgputil.cache;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Represents an ItemCache whose items should be explicitly added.  Implements the Map interface
 * for familiarity but iteration through members is not supported.  Items in the ManagedMap are
 * purged using trimming characteristics passed to constructor, or default if none are passed.
 * 
 * @author rdoherty
 *
 * @param <S> type of key
 * @param <T> type of value
 */
public class ManagedMap<S,T> extends ItemCache<S,T> implements Map<S,T> {

  // fetcher used to insert a new item into the cache
  private NoUpdateItemFetcher<S,T> getValueFetcher(T value){
    return key -> value;
  }

  // fetcher used to check presence of a key in the cache
  private NoUpdateItemFetcher<S,T> getNotFoundFetcher() {
    return key -> {
      throw new UnfetchableItemException("Key not found in managed map.");
    };
  }

  /**
   * Creates a managed map with default settings.
   */
  public ManagedMap() {
    super();
  }

  /**
   * Creates a managed map with custom max-size and trimming characteristics.
   * 
   * @param maxCachedItems maximum number of objects that can be cached
   * @param numToTrimOnCapacity number of objects to trim when capacity reached
   */
  public ManagedMap(int maxCachedItems, int numToTrimOnCapacity) {
    super(maxCachedItems, numToTrimOnCapacity);
  }

  /**
   * @return number of items currently stored in the map
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
   * @param possible key to item in the map
   * @return true if the passed key represents an item in the map, else false
   */
  @SuppressWarnings("unchecked")
  @Override
  public boolean containsKey(Object key) {
    try {
      getItem((S)key, getNotFoundFetcher());
      return true;
    }
    catch (UnfetchableItemException e) {
      return false;
    }
  }

  /**
   * Unsupported due to ItemCache limitations
   */
  @Override
  public boolean containsValue(Object value) {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns the item associated with the passed key
   * 
   * @param possible key to item in the map
   * @return the item associated with the passed key, or null if that key does not exist
   */
  @SuppressWarnings("unchecked")
  @Override
  public T get(Object key) {
    try {
      return getItem((S)key, getNotFoundFetcher());
    }
    catch (UnfetchableItemException e) {
      return null;
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
    try {
      T oldValue = get(key);
      if (oldValue != null) {
        remove(key);
      }
      getItem(key, getValueFetcher(value));
      return value;
    }
    catch (UnfetchableItemException e) {
      // this should never happen
      throw new RuntimeException(e);
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
    T oldValue = get(key);
    expireCachedItems((S)key);
    return oldValue;
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
   * Unsupported due to ItemCache limitations
   */
  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported due to ItemCache limitations
   */
  @Override
  public Set<S> keySet() {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported due to ItemCache limitations
   */
  @Override
  public Collection<T> values() {
    throw new UnsupportedOperationException();
  }

  /**
   * Unsupported due to ItemCache limitations
   */
  @Override
  public Set<java.util.Map.Entry<S, T>> entrySet() {
    throw new UnsupportedOperationException();
  }

}
