package org.gusdb.fgputil;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Wrapper around a reference to a typed Object.  Sometimes while writing anonymous functions we must make
 * variables final that we might not otherwise in order to close over them.  In some cases making the object
 * final is not feasible since not only the properties of the object may change, but the object itself may
 * change value.  It may also be desirable to close over a primitive or immutable object (say, a String) which
 * may change value.  This class is a way around these limitations since you can create a final instance of
 * Wrapper and place your mutable object inside, changing its value at will either inside or outside your
 * closing class.  This class is threadsafe but that does not protect mutations on the enclosed object.
 * 
 * @author rdoherty
 *
 * @param <T> the type of the object wrapped
 */
public class Wrapper<T> {

  private T _obj = null;
  private ReadWriteLock _lock = new ReentrantReadWriteLock();

  public Wrapper() { }

  public Wrapper<T> set(T obj) {
    try {
      _lock.writeLock().lock();
      _obj = obj;
      return this;
    }
    finally {
      _lock.writeLock().unlock();
    }
  }

  public T get() {
    try {
      _lock.readLock().lock();
      return _obj;
    }
    finally {
      _lock.readLock().unlock();
    }
  }
}
