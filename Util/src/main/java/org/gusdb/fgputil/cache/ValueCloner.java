package org.gusdb.fgputil.cache;

/**
 * Clones an object in a cache-safe way.  Any type of object can be cached, but
 * sometimes measures must be taken to ensure that an object returned from the
 * cache is not modified in a way that affects the cached version.  The creator
 * of the cache can ensure this by passing an ValueCloner implementation that
 * disallows cached object modification.
 * 
 * This can be accomplished in a variety of ways:
 * <ul>
 *   <li>an implementation of this class could return immutable objects</li>
 *   <li>an implementation of this class could return deep clones</li>
 *   <li>the cached objects could be immutable themselves</li>
 *   <li>the cached objects could be read-only interfaces to mutable objects</li>
 * </ul>
 * 
 * Using a custom implementation of this interface gives the cache user the
 * power to control how they want to protect the objects in the cache from
 * modification after retrieval.
 * 
 * @author rdoherty
 * @param <T> type of object to clone
 */
public interface ValueCloner<T> {

  /**
   * Returns an object representing the passed object.  The returned object will
   * be returned by the cache instead of the "real" cached object.
   * 
   * @param cachedValue object stored in the cache
   * @return object returned by the cache instead of the cached object itself
   */
  public T createCachesafeClone(T cachedValue);

}
