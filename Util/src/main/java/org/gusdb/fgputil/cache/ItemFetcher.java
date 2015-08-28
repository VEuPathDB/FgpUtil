package org.gusdb.fgputil.cache;

public interface ItemFetcher<S,T> {

  public T fetchItem(S id, T previousVersion) throws UnfetchableItemException;
  public boolean itemNeedsUpdating(T item);

}
