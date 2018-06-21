package org.gusdb.fgputil.cache;

/**
 * Creates and updates objects stored in a cache
 * 
 * @author rdoherty
 *
 * @param <S> type of cache key
 * @param <T> type of cached value
 */
@FunctionalInterface
public interface ValueFactory<S,T> {

  /**
   * Creates a new value to be stored in a cache.  This method is called when a
   * requested value's key does not yet exist in the cache.
   * 
   * @param key key of the value to be created
   * @return a new value for the passed key
   * @throws ValueProductionException if value cannot be created
   */
  public T getNewValue(S key) throws ValueProductionException;

  /**
   * Updates a value if needed.  This method is called when an object has been
   * requested which already exists in the cache, but for which this class's
   * <code>valueNeedsUpdating(T)</code> method returned true.  This method can
   * safely modify and return the previous value if it wishes, or create a new
   * one.  If <code>valueNeedsUpdating(T)</code> returns false (the default behavior),
   * this method will not be called; thus, the default version of this method
   * throws UnsupportedOperationException.
   * 
   * @param key of the item to be updated
   * @param previousValue currently cached value behind the passed key
   * @return an updated value
   * @throws ValueProductionException if value cannot be updated
   */
  public default T getUpdatedValue(S key, T previousValue) throws ValueProductionException {
    throw new UnsupportedOperationException(
        "This should never be called since itemNeedsUpdating() always returns false.");
  }

  /**
   * Returns true if the value passed is out of date or is insufficient for the
   * purposes of this value factory.  This method is called when an value requested
   * is currently cached.  If this method returns true, then this object's
   * <code>getUpdatedValue(S,T)</code> method will be called; otherwise, the currently
   * cached value will be returned.  By default, this method returns false (i.e.
   * values are never updated).  If a subclass overrides this behavior, it should
   * also override <code>getUpdatedValue(S,T)</code> or a UnsupportedOperationException
   * will result.
   * 
   * @param value the currently cached value
   * @return whether the value is still current and sufficient for the current purpose
   */
  public default boolean valueNeedsUpdating(T value) {
    return false;
  }

}
