package org.gusdb.fgputil.iterator;

import java.util.Iterator;

public interface CloseableIterator<T> extends AutoCloseable, Iterator<T> {

  static <T> CloseableIterator<T> of(Iterator<T> iterator) {
    return new CloseableIterator<>() {
      @Override
      public void close() throws Exception {
        // No op when wrapping around a normal iterator.
      }

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public T next() {
        return iterator.next();
      }
    };
  }
}
