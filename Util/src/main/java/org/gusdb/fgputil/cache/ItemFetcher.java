package org.gusdb.fgputil.cache;

/**
 * Fetches and updates objects stored in an ItemCache
 * 
 * @author rdoherty
 *
 * @param <S> type of ID
 * @param <T> type of cached item
 */
public interface ItemFetcher<S,T> {

  /**
   * Fetches an item.  This method is called when an item is requested which
   * does not yet exist in the cache.
   * 
   * @param id ID of the item to be fetched
   * @return fetched item
   * @throws UnfetchableItemException if item cannot be fetched
   */
  public T fetchItem(S id) throws UnfetchableItemException;

  /**
   * Updates an item if needed.  This method is called when an item has been
   * requested which already exists in the cache, but for which this class's
   * <code>itemNeedsUpdating(T)</code> method returned true.
   * 
   * @param id ID of the item to be updated
   * @param previousVersion currently cached version behind the ID
   * @return an updated version of the item
   * @throws UnfetchableItemException if item cannot be updated
   */
  public T updateItem(S id, T previousVersion) throws UnfetchableItemException;

  /**
   * Returns true if the item requested is insufficient for the current purpose.
   * This method is called when an item is requested that is currently cached.
   * If this method returns false, then this object's <code>updateItem(S,T)</code>
   * method will be called; otherwise, the currently cached item will be returned.
   * 
   * @param item the currently cached item
   * @return whether the item is sufficient for the current purpose
   */
  public boolean itemNeedsUpdating(T item);

}
