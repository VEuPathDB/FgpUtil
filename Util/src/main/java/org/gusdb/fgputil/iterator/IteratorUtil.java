package org.gusdb.fgputil.iterator;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.gusdb.fgputil.Wrapper;

public class IteratorUtil {

  /**
   * Converts an iterator of one type to an iterator of another, given a conversion function
   *
   * @param <T> type of original iterator
   * @param <S> type of new iterator
   * @param iterator original iterator
   * @param transformer function to apply to each element before returning in new iterator
   */
  public static <T,S> Iterator<S> transform(final Iterator<T> iterator, final Function<T,S> transformer) {
    return new Iterator<S>() {

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public S next() {
        return transformer.apply(iterator.next());
      }

      @Override
      public void remove() {
        iterator.remove();
      }
    };
  }

  /**
   * Converts an iterator over a Collection of items to an iterator over all the items in all the collections.
   * The iterator will iterate over the items in the first collection, then the second, until all collections
   * are exhausted.  No null Collections are allowed in the passed iterator; a null value will result in a
   * NullPointerException.  However, null items inside the collections will be returned as is.
   * 
   * @param collectionIter interator over a set of collections
   * @return iterator over the items in the collections
   * @throws NullPointerException if the passed iterator or any of the collections it iterates over are null
   */
  public static <T> Iterator<T> flatten(final Iterator<Collection<T>> collectionIter) {
    // create and initialize wrapper
    final Wrapper<Iterator<T>> wrapper = new Wrapper<>();
    if (collectionIter.hasNext()) {
      wrapper.set(collectionIter.next().iterator());
    }
    return new Iterator<T>() {

      @Override
      public boolean hasNext() {
        if (wrapper.get() == null) {
          // means there were no more collections in collectionIter
          return false;
        }
        // iterator value in wrapper; try to determine if there are any more items
        while (true) {
          // if current collection has next another value the use it
          if (wrapper.get().hasNext()) {
            return true;
          }
          // no more items in current collection; if no more collections then no more items
          if (!collectionIter.hasNext()) {
            return false;
          }
          // no more items in current collection and there is another collection; move to it
          wrapper.set(collectionIter.next().iterator());
        }
      }

      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException();
        }
        return wrapper.get().next();
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException("The remove() method is not supported by this iterator.");
      }
    };
  }

  /**
   * Transforms a Cursor to an Iterator.  This requires caching the "next" object in memory, so be advised
   * these should probably not be too big.  The object behind the cursor is not closed in any way; the
   * caller is responsible for closing any underlying open resources.
   * 
   * @param cursor cursor object
   * @return a read-only iterator facade over the passed cursor
   */
  public static <T> ReadOnlyIterator<T> toIterator(final Cursor<T> cursor) {

    final Wrapper<Boolean> storedItemPresent = new Wrapper<Boolean>().set(cursor.next());
    final Wrapper<T> storedItem = new Wrapper<T>().set(storedItemPresent.get() ? cursor.get() : null);

    return new ReadOnlyIterator<T>() {
      @Override
      public boolean hasNext() {
        return storedItemPresent.get();
      }
      @Override
      public T next() {
        if (!hasNext()) {
          throw new NoSuchElementException("No more elements can be returned by this iterator.");
        }
        T nextItem = storedItem.get();
        storedItemPresent.set(cursor.next());
        storedItem.set(storedItemPresent.get() ? cursor.get() : null);
        return nextItem;
      }
    };
  }

  /**
   * Transforms an Iterator to a Cursor.  This requires caching the "next" object in memory, so be advised
   * these should probably not be too big.  The object behind the cursor is not closed in any way; the
   * caller is responsible for closing any underlying open resources.
   * 
   * @param iterator iterator object
   * @return a cursor facade over the passed iterator
   */
  public static <T> Cursor<T> toCursor(final Iterator<T> iterator) {
    final Wrapper<T> currentItem = new Wrapper<>();
    final Wrapper<Boolean> hasCurrentItem = new Wrapper<Boolean>().set(false);
    return new Cursor<T>() {

      @Override
      public boolean next() {
        if (iterator.hasNext()) {
          hasCurrentItem.set(false);
          return false;
        }
        currentItem.set(iterator.next());
        return true;
      }

      @Override
      public T get() {
        if (!hasCurrentItem.get()) {
          throw new NoSuchElementException("Cursor is not currently pointing to any element.");
        }
        return currentItem.get();
      }
    };
  }

  /**
   * Returns an Iterable wrapper around the passed iterator.  Can be used as a convenience when you have
   * an iterator in hand and want to pass over it with a for-each loop, or if you have a utility that takes
   * an Iterable.
   * 
   * @param iterator iterator object
   * @return an iterable that returns the passed iterator from its iterator() method
   */
  public static <T> Iterable<T> toIterable(final Iterator<T> iterator) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return iterator;
      }
    };
  }
}
