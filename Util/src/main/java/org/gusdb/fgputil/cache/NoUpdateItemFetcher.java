package org.gusdb.fgputil.cache;

/**
 * Abstract implementation of ItemFetcher that will never update the item in the cache.  It is useful in
 * situations where an item never "expires" on its own merit, and thus once in the cache it is valid unless
 * explicitly removed or trimmed to reduce cache size.
 * 
 * @author rdoherty
 *
 * @param <S> type of cache lookup key
 * @param <T> type of value stored in cache
 */
public abstract class NoUpdateItemFetcher<S,T> implements ItemFetcher<S,T>{

  /**
   * Will not be called by ItemCache since itemNeedsUpdating (below) always returns false
   */
  @Override
  public T updateItem(S id, T previousVersion) throws UnfetchableItemException {
    throw new UnsupportedOperationException(
        "This should never be called since itemNeedsUpdating() always returns false.");
  }

  /**
   * @return false
   */
  @Override
  public boolean itemNeedsUpdating(T item) {
    return false;
  }

}
