package org.gusdb.fgputil.cache;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

/**
 * Implements an efficient cache of objects mapped by key.  This class is
 * thread-safe and can be used in a static, application-wide way to cache
 * homogeneous objects.
 * 
 * @param S type of unique keys of values cached
 * @param T type of values cached
 * 
 * @author rdoherty
 */
public class InMemoryCache<S,T> {

  private static final Logger LOG = Logger.getLogger(InMemoryCache.class);

  /**
   * Default capacity.  When a value is requested that will cause the size
   * to exceed the capacity, cache trimming occurs.
   */
  public static final int DEFAULT_CAPACITY = 1000;

  /**
   * Default number of entries trimmed from cache when capacity is exceeded.
   */
  public static final int DEFAULT_NUM_TO_TRIM_ON_CAPACITY = 700;

  private static class ValueContainer<T> {
    Lock lock = new ReentrantLock();
    T value = null;
  }
  
  private final Map<S, ValueContainer<T>> _cache = new LinkedHashMap<>();
  private final Lock _cacheLock = new ReentrantLock();
  private final int _capacity;
  private final int _numToTrimOnCapacity;
  private final ValueCloner<T> _cloner;
  private Date _lastTrimDate = null;

  /**
   * Creates a cache with default settings.
   */
  public InMemoryCache() {
    this(DEFAULT_CAPACITY, DEFAULT_NUM_TO_TRIM_ON_CAPACITY, null);
  }

  /**
   * Creates a cache with custom capacity and trimming characteristics.
   * 
   * @param capacity maximum number of objects that can be cached
   * @param numToTrimOnCapacity number of objects to trim when capacity reached
   */
  public InMemoryCache(int capacity, int numToTrimOnCapacity) {
    this(capacity, numToTrimOnCapacity, null);
  }

  /**
   * Creates a cache with default capacity and trimming but with a custom cloner.
   * The cloner provides a mechanism to return "safe" copies of objects to
   * calling code (i.e. objects that cannot impact the cached versions).  These
   * could be clones, deep clones, immutable versions of cached objects, or
   * copies that share immutable data but copy mutable data, etc.  The default
   * cloner simply returns the cached object as-is, which is safe in many
   * circumstances.
   * 
   * @param cloner custom cloner
   */
  public InMemoryCache(ValueCloner<T> cloner) {
    this(DEFAULT_CAPACITY, DEFAULT_NUM_TO_TRIM_ON_CAPACITY, cloner);
  }

  /**
   * Creates a cache with custom capacity and trimming and with a custom cloner.
   * The cloner provides a mechanism to return "safe" copies of objects to
   * calling code (i.e. objects that cannot impact the cached versions).  These
   * could be clones, deep clones, immutable versions of cached objects, or
   * copies that share immutable data but copy mutable data, etc.  The default
   * cloner simply returns the cached object as-is, which is safe in many
   * circumstances.
   * 
   * @param capacity maximum number of objects that can be cached
   * @param numToTrimOnCapacity number of objects to trim when capacity reached
   * @param cloner custom cloner
   */
  public InMemoryCache(int capacity, int numToTrimOnCapacity, ValueCloner<T> cloner) {

    // can store 1 entry but not fewer
    _capacity = Math.max(1, capacity);

    // don't trim more than the max number of entries, but trim at least 1
    _numToTrimOnCapacity = Math.max(1, Math.min(capacity, numToTrimOnCapacity));

    // cloner used to return cache-safe objects to callers
    _cloner = (cloner != null ? cloner : cachedItem -> cachedItem);
  }

  /**
   * Returns the maximum size of the cache (i.e. number of entries).  Actual memory
   * size will vary based on the size of the items stored and their keys.
   * 
   * @return maximum size of the cache
   */
  public int getCapacity() {
    return _capacity;
  }

  /**
   * Returns the number of entries to be trimmed off the cache (least recently
   * accessed trimmed first) when the capacity is reached.
   * 
   * @return number of entries trimmed from cache if capacity is reached
   */
  public int getNumToTrimOnCapacity() {
    return _numToTrimOnCapacity;
  }

  /**
   * Returns date of last capacity trim (individual items may be marked invalid
   * for other reasons at other times).
   * 
   * @return date of last capacity trim
   */
  public Date getLastTrimDate() {
    return _lastTrimDate;
  }

  /**
   * Retrieves an item from the cache with the passed key.  If the item is not
   * in the cache (not yet fetched, expired, or trimmed), it will be fetched
   * with the passed ValueFactory.
   * 
   * @param key key of the desired item
   * @param factory factory for items of the type cached
   * @return object represented by the passed key
   * @throws ValueProductionException if unable to create/update the item
   */
  public T getValue(S key, ValueFactory<S,T> factory) throws ValueProductionException {
    ValueContainer<T> container = getValueContainerById(key);
    T result = null;
    try {
      container.lock.lock();

      // if no value exists in the cache for this key, create it
      if (container.value == null) {
        try {
          container.value = factory.getNewValue(key);
        }
        catch (Exception e) {
          // if creation fails, remove the container for the next attempt
          try {
            _cacheLock.lock();
            _cache.remove(key);
          }
          finally {
            _cacheLock.unlock();
          }
          throw convertException(e);
        }
      }

      // otherwise check to see if value needs updating before returning
      else if (factory.valueNeedsUpdating(container.value)) {
        try {
          container.value = factory.getUpdatedValue(key, container.value);
        }
        catch (Exception e) {
          // if update fails, make a note in the log, but leave the old version
          //   in the cache and throw exception
          LOG.warn("ItemFetcher of type " + factory.getClass().getName() +
              " failed to update " + container.value.getClass().getName() +
              " with ID " + key, e.getCause());
          throw convertException(e);
        }
      }

      // push entry to the "back" of the linked hash map to
      //   keep least frequently used items in front
      try {
        _cacheLock.lock();
        _cache.remove(key);
        // value may have been removed while we were creating it; assume this
        //   new version is up-to-date enough and reinsert
        _cache.put(key, container);
      }
      finally {
        _cacheLock.unlock();
      }
      result = _cloner.createCachesafeClone(container.value);
    }
    finally {
      container.lock.unlock();
    }
    return result;
  }

  private ValueProductionException convertException(Exception e) {
    return (e instanceof ValueProductionException ?
        (ValueProductionException) e : new ValueProductionException(e));
  }

  // Returns an empty value container placed in the map at the given key
  private ValueContainer<T> getValueContainerById(S key) {
    ValueContainer<T> container = _cache.get(key);
    if (container == null) {
      try {
        _cacheLock.lock();
        // check again to see if value added while waiting for cache access
        container = _cache.get(key);
        if (container == null) {
          // empty value container
          container = new ValueContainer<T>();
          _cache.put(key, container);
          checkCapacity();
        }
      }
      finally {
        _cacheLock.unlock();
      }
    }
    return container;
  }

  // assumes cache is locked
  private void checkCapacity() {
    if (_cache.size() > _capacity) {
      // we know least recently accessed entries appear first in map iteration
      List<S> allKeys = new ArrayList<>(_cache.keySet());
      int numToTrim = Math.min(_numToTrimOnCapacity, _cache.size());
      LOG.debug("Capacity reached, will trim " + numToTrim + " cached items.");
      _lastTrimDate = new Date();
      for (int i = 0; i < numToTrim; i++) {
        S id = allKeys.get(i);
        LOG.debug("Trimming item with ID " + id);
        _cache.remove(id);
      }
    }
  }

  /**
   * Removes the entries behind the passed keys from the cache.
   * 
   * @param ids IDs of items to expire
   */
  public void expireEntries(@SuppressWarnings("unchecked") S... ids) {
    try {
      _cacheLock.lock();
      for (S id : ids) {
        LOG.debug("Expiring item with ID " + id);
        _cache.remove(id);
      }
    }
    finally {
      _cacheLock.unlock();
    }
  }

  /**
   * Returns the number of entries in the cache
   * 
   * @return the number of entries in the cache
   */
  public int getSize() {
    return _cache.size();
  }
}

