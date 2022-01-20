package org.gusdb.fgputil.interview;

import java.util.Iterator;
import java.util.List;

/**
 * Takes two iterators over "records" (maybe parsed tabular data?) and merges
 * the fields of parent and child records to produce child records which
 * "inherit" the properties of their parent.  Implementation can assume:
 *
 * 1. First item in a parent record is the parent's ID
 * 2. Parent stream is ordered, ascending, by parent ID
 *
 * 3. First item in a child record is the child's ID
 * 4. Second item in a child record is the child's parent's ID
 * 5. Child stream is ordered, ascending, by parent ID, then by child ID
 *
 * Additionally, a parent record is present IFF at least one of its child
 * records is present.
 *
 * For example, these two streams:
 *
 * Parent records:
 *   [ PID1, a, b, c ]
 *   [ PID2, d, e, f ]
 *
 * Child records:
 *   [ CID1, PID1, 1, 2 ]
 *   [ CID2, PID1, 3, 4 ]
 *   [ CID3, PID2, 5, 6 ]
 *
 * Should produce the following:
 *
 * Merged records:
 *   [ CID1, PID1, 1, 2, a, b, c ]
 *   [ CID2, PID1, 3, 4, a, b, c ]
 *   [ CID3, PID2, 5, 6, d, e, f ]
 *
 */
public class MergedStream implements Iterator<List<String>> {

  @SuppressWarnings("unused")
  private final Iterator<List<String>> _parentStream;
  @SuppressWarnings("unused")
  private final Iterator<List<String>> _childStream;

  public MergedStream(Iterator<List<String>> parentStream, Iterator<List<String>> childStream) {
    _parentStream = parentStream;
    _childStream = childStream;
  }

  @Override
  public boolean hasNext() {
    // fill me in!
    return false;
  }

  @Override
  public List<String> next() {
    // fill me in!
    return null;
  }
}

/*******************************************************************************
 * Less complicated solution if constraints in description are followed
 *******************************************************************************

  /** With Additionally in place
  @Override
  public boolean hasNext() {
    return _childStream.hasNext();
  }

  @Override
  public List<String> next() {
    List<String> child = _childStream.next();
    if (_cachedParent == null || !_cachedParent.get(0).equals(child.get(1))) {
      _cachedParent = _parentStream.next();
    }
    List<String> mergedRecord = new ArrayList<>(child);
    for (int i = 2; i < _cachedParent.size(); i++) {
      mergedRecord.add(_cachedParent.get(i));
    }
    return mergedRecord;
  }
*/

/*******************************************************************************
 * More complicated solution if 'additionally' statement taken out
 *   i.e. must facilitate an inner join to exclude extra rows in both streams
 *******************************************************************************

  private List<String> _nextJoinedRecord;
  private List<String> _cachedParent;

  public MergedStream(Iterator<List<String>> parentStream, Iterator<List<String>> childStream) {
    _parentStream = parentStream;
    _childStream = childStream;

    // cache first child/parent match and associated parent record
    if (_parentStream.hasNext()) {
      _cachedParent = _parentStream.next();
      findNextJoinedRecord();
    }
  }

  @Override
  public boolean hasNext() {
    return _nextJoinedRecord != null;
  }

  @Override
  public List<String> next() {
    if (!hasNext())
      throw new NoSuchElementException();
    List<String> tmp = _nextJoinedRecord;
    findNextJoinedRecord();
    return tmp;
  }

  private void findNextJoinedRecord() {
    // we know cachedParent != null here

    // set next joined record to null (i.e. no more records) until the next is found
    _nextJoinedRecord = null;

    // load next child for consideration
    if (!_childStream.hasNext()) {
      return;
    }
    List<String> child = _childStream.next();

    // loop until:
    //    a) next match is found
    //    b) run out of parent records
    //    c) run out of child records
    while (true) {
      int compare = child.get(1).compareTo(_cachedParent.get(0));
      if (compare < 0) {
        // iterate child
        if (!_childStream.hasNext()) {
          return; // out of children
        }
        child = _childStream.next();
      }
      else if (compare > 0) {
        // iterate parent
        if (!_parentStream.hasNext()) {
          return; // out of parents
        }
        _cachedParent = _parentStream.next();
      }
      else {
        // found next match; build joined record and return
        _nextJoinedRecord = new ArrayList<>(child);
        for (int i = 2; i < _cachedParent.size(); i++) {
          _nextJoinedRecord.add(_cachedParent.get(i));
        }
        return;
      }
    }
  }
*******************************************************************************/
