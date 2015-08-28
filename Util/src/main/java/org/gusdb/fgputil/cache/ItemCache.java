package org.gusdb.fgputil.cache;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.log4j.Logger;

/**
 * Implements an efficient cache of objects mapped by integer ID.  This class is
 * thread-safe and can be used in a static, application-wide way to cache
 * homogeneous objects.
 * 
 * @author rdoherty
 */
public class ItemCache<S,T> {

  private static final Logger LOG = Logger.getLogger(ItemCache.class);

  /**
   * Default maximum size.  When an item is requested that will cause this size
   * to be exceeded, cache trimming occurs.
   */
  public static final int DEFAULT_MAX_CACHED_ITEMS = 1000;

  /**
   * Default number of items trimmed from cache when capacity is exceeded.
   */
  public static final int DEFAULT_NUM_TO_TRIM_ON_CAPACITY = 700;

  private static class ItemContainer<T> {
    Lock lock = new ReentrantLock();
    T item = null;
  }
  
  private final Map<S, ItemContainer<T>> _cache = new LinkedHashMap<>();
  private final Lock _cacheLock = new ReentrantLock();
  private final int _maxCachedItems;
  private final int _numToTrimOnCapacity;
  private final ItemCloner<T> _cloner;

  /**
   * Creates a cache with default settings.
   */
  public ItemCache() {
    this(DEFAULT_MAX_CACHED_ITEMS, DEFAULT_NUM_TO_TRIM_ON_CAPACITY, null);
  }

  /**
   * Creates a cache with custom max-size and trimming characteristics.
   * 
   * @param maxCachedItems maximum number of objects that can be cached
   * @param numToTrimOnCapacity number of objects to trim when capacity reached
   */
  public ItemCache(int maxCachedItems, int numToTrimOnCapacity) {
    this(maxCachedItems, numToTrimOnCapacity, null);
  }

  /**
   * Creates a cache with default max-size and trimming but with a custom cloner.
   * The cloner provides a mechanism to return "safe" copies of objects to
   * calling code (i.e. objects that cannot impact the cached versions).  These
   * could be clones, deep clones, immutable versions of cached objects, or
   * copies that share immutable data but copy mutable data, etc.  The default
   * cloner simply returns the cached object as-is, which is safe in many
   * circumstances.
   * 
   * @param cloner custom cloner
   */
  public ItemCache(ItemCloner<T> cloner) {
    this(DEFAULT_MAX_CACHED_ITEMS, DEFAULT_NUM_TO_TRIM_ON_CAPACITY, cloner);
  }

  /**
   * Creates a cache with custom max-size and trimming and with a custom cloner.
   * The cloner provides a mechanism to return "safe" copies of objects to
   * calling code (i.e. objects that cannot impact the cached versions).  These
   * could be clones, deep clones, immutable versions of cached objects, or
   * copies that share immutable data but copy mutable data, etc.  The default
   * cloner simply returns the cached object as-is, which is safe in many
   * circumstances.
   * 
   * @param maxCachedItems maximum number of objects that can be cached
   * @param numToTrimOnCapacity number of objects to trim when capacity reached
   * @param cloner custom cloner
   */
  public ItemCache(int maxCachedItems, int numToTrimOnCapacity, ItemCloner<T> cloner) {

    // can store 0 items but not fewer
    _maxCachedItems = Math.max(0, maxCachedItems);

    // don't trim more than the max number of items, but trim at least 1
    _numToTrimOnCapacity = Math.max(1, Math.min(maxCachedItems, numToTrimOnCapacity));

    // cloner used to return cache-safe objects to callers
    _cloner = (cloner != null ? cloner : new ItemCloner<T>(){
      @Override
      public T createCachesafeClone(T cachedItem) {
        return cachedItem;
      }
    });
  }

  /**
   * Retrieves an item from the cache with the passed ID.  If the item is not
   * in the cache (not yet fetched, expired, or trimmed), it will be fetched
   * with the passed ItemFetcher.
   * 
   * @param id ID of the desired item
   * @param fetcher fetcher for items of the type cached
   * @return item represented by the passed ID
   * @throws UnfetchableItemException if unable to fetch the item
   */
  public T getItem(S id, ItemFetcher<S,T> fetcher) throws UnfetchableItemException {
    ItemContainer<T> container = getItemContainerById(id);
    T result = null;
    try {
      container.lock.lock();
      if (container.item == null || fetcher.itemNeedsUpdating(container.item)) {
        container.item = fetcher.fetchItem(id);
      }
      // push item to the "back" of the linked hash map to
      //   keep least frequently used items in front
      try {
        _cacheLock.lock();
        _cache.remove(id);
        // item may have been removed while we were fetching it; assume this
        //   new version is up-to-date enough and reinsert
        _cache.put(id, container);
      }
      finally {
        _cacheLock.unlock();
      }
      result = _cloner.createCachesafeClone(container.item);
    }
    finally {
      container.lock.unlock();
    }
    return result;
  }

  // Returns  an empty item container placed in the map at the given ID
  private ItemContainer<T> getItemContainerById(S id) {
    ItemContainer<T> item = _cache.get(id);
    if (item == null) {
      try {
        _cacheLock.lock();
        // check again to see if item added while waiting for cache access
        item = _cache.get(id);
        if (item == null) {
          // empty item container
          item = new ItemContainer<T>();
          _cache.put(id, item);
          checkCapacity();
        }
      }
      finally {
        _cacheLock.unlock();
      }
    }
    return item;
  }

  // assumes cache is locked
  private void checkCapacity() {
    if (_cache.size() > _maxCachedItems) {
      // we know oldest records will appear first in map iteration
      List<S> stepIdsToTrim = new ArrayList<>(_cache.keySet());
      int numToTrim = Math.min(_numToTrimOnCapacity, _cache.size());
      LOG.debug("Capacity reached, will trim " + numToTrim + " cached items.");
      for (int i = 0; i < numToTrim; i++) {
        S id = stepIdsToTrim.get(i);
        LOG.debug("Trimming item with ID " + id);
        _cache.remove(id);
      }
    }
  }

  /**
   * Removes the items behind the passed IDs from the cache.
   * 
   * @param ids IDs of items to expire
   */
  public void expireCachedItems(@SuppressWarnings("unchecked") S... ids) {
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
}

