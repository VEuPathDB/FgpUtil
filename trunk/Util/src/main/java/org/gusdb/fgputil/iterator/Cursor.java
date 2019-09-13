package org.gusdb.fgputil.iterator;

/**
 * The Iterator interface has two read methods: hasNext() and next().  Typically hasNext() is called first and if true is returned, next() can be called to fetch the next item (and move the iterator).
 * However, it is sometimes more convenient to implement a JDBC ResultSet-style interface where an attempt is
 * made to fetch the next object (moving the iterator up front), and then the current object can be accessed
 * over and over.  This interface codifies the second case.  The IteratorUtil class has methods to translate
 * from one interface to the other and back.
 * 
 * @author rdoherty
 *
 * @param <T> type of object iterating over
 */
public interface Cursor<T> {

  public boolean next();

  public T get();

}
