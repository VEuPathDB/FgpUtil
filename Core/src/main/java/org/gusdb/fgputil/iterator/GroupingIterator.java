package org.gusdb.fgputil.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Converts an Iterator of objects sorted in a particular way and groups them
 * up, returning them one-by-one using the Iterator API.
 * 
 * @author rdoherty
 *
 * @param <T> type of objects passed in 
 */
public class GroupingIterator<T> implements Iterator<List<T>> {

  private final Iterator<T> _source;
  private final BiFunction<T,T,Boolean> _isMatch;

  // next object in the stream which has not yet been grouped
  // null if hasNext has not been called or if the input iterator has been exhausted
  private T _next;

  public GroupingIterator(Iterator<T> source, BiFunction<T,T,Boolean> isMatch) {
    _source = source;
    _isMatch = isMatch;
  }

  @Override
  public boolean hasNext() {
    return _next != null || _source.hasNext();
  }

  @Override
  public List<T> next() {

    // if no more groups, return null
    if (!hasNext()) return null;

    // if _next not yet populated, load with next input object
    if (_next == null) _next = _source.next();

    // build next group of objects; all will "match" the first
    List<T> nextGroup = new ArrayList<>();
    nextGroup.add(_next);
    T first = _next;
    while (_source.hasNext()) {
      _next = _source.next();
      if (_isMatch.apply(first, _next)) {
        nextGroup.add(_next);
      }
      else {
        // more objects, but no match; return this group
        return nextGroup;
      }
    }

    // source ran out of objects
    _next = null;
    return nextGroup;
  }

}
