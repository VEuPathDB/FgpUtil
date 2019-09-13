package org.gusdb.fgputil.iterator;

import java.util.Iterator;

public abstract class ReadOnlyIterator<T> implements Iterator<T> {

  @Override
  public void remove() {
    throw new UnsupportedOperationException("Unable to remove items using this iterator.");
  }

}
