package org.gusdb.fgputil.cache;

public interface ItemFetcher<S,T> {

  public T fetchItem(S id) throws UnfetchableItemException;
  public boolean itemNeedsUpdating(T item);

}
