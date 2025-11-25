package org.gusdb.fgputil.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.FunctionalInterfaces.TriFunction;
import org.gusdb.fgputil.functional.Functions;

/**
 * Iterator which combines two iterators of different types from which a shared
 * ordered identifier can be extracted.  Objects from both iterators that share
 * an ID are collected into Lists and fed to a combiner function that produces
 * the objects returned by this iterator.
 * 
 * @author rdoherty
 *
 * @param <L> type returned by left iterator
 * @param <R> type returned by right iterator
 * @param <I> type of ordered identifier extracted from both L and R to order join
 * @param <T> type returned by this iterator
 */
public class OrderedJoinIterator<L,R,I extends Comparable<I>,T> implements Iterator<T> {

  // final fields passed into constructor
  private final Iterator<L> _leftIter;
  private final Iterator<R> _rightIter;
  private final Function<L,I> _getLeftIdentifier;
  private final Function<R,I> _getRightIdentifier;
  private final TriFunction<I, List<L>,List<R>,T> _combinerFunction;

  // mutable state fields
  private TwoTuple<L,I> _currentLeft;
  private TwoTuple<R,I> _currentRight;

  public OrderedJoinIterator(
      final Iterator<L> leftIter,
      final Iterator<R> rightIter,
      final Function<L,I> getLeftIdentifier,
      final Function<R,I> getRightIdentifier,
      final TriFunction<I, List<L>,List<R>,T> combinerFunction) {
    _leftIter = leftIter;
    _rightIter = rightIter;
    _getLeftIdentifier = getLeftIdentifier;
    _getRightIdentifier = getRightIdentifier;
    _combinerFunction = combinerFunction;
    _currentLeft = readNextOrNull(_leftIter, _getLeftIdentifier);
    _currentRight = readNextOrNull(_rightIter, _getRightIdentifier);
    
  }

  @Override
  public boolean hasNext() {
    return _currentLeft != null || _currentRight != null;
  }

  @Override
  public T next() {
    // find next ID; if both left and right are null, will throw NoSuchElementException
    I identifier = getNextIdentifier(_currentLeft, _currentRight);

    // load up values on both sides that have this identifier and set first one that does not
    List<L> leftObjects = new ArrayList<>();
    _currentLeft = loadObjectsWithId(_currentLeft, identifier, _leftIter, _getLeftIdentifier, leftObjects);
    List<R> rightObjects = new ArrayList<>();
    _currentRight = loadObjectsWithId(_currentRight, identifier, _rightIter, _getRightIdentifier, rightObjects);

    return _combinerFunction.apply(identifier, leftObjects, rightObjects);
  }

  private <S> TwoTuple<S, I> loadObjectsWithId(
      TwoTuple<S, I> currentValue,
      I idToMatch,
      Iterator<S> iter,
      Function<S, I> getIdentifier,
      List<S> matchingObjects) {
    while (currentValue != null && currentValue.getSecond().compareTo(idToMatch) == 0) {
      matchingObjects.add(currentValue.getFirst());
      currentValue = readNextOrNull(iter, getIdentifier);
    }
    return currentValue;
  }

  private <S> TwoTuple<S,I> readNextOrNull(Iterator<S> iter, Function<S,I> getIdentifier) {
    if (!iter.hasNext()) {
      return null;
    }
    S value = iter.next();
    I identifier = getIdentifier.apply(value);
    return new TwoTuple<>(value, identifier);
  }

  private I getNextIdentifier(TwoTuple<L,I> left, TwoTuple<R,I> right) {
    I id = right == null
      ? left == null
        ? Functions.doThrow(() -> new NoSuchElementException())
        : left.getSecond()
      : left == null
        ? right.getSecond()
        : left.getSecond().compareTo(right.getSecond()) <= 0
          ? left.getSecond()
          : right.getSecond();
    return id;
  }
}
