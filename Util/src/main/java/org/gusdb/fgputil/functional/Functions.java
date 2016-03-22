package org.gusdb.fgputil.functional;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;

public class Functions {

  public static <S,T> List<T> mapToList(Iterable<S> inputs, Function<S,T> function) {
    List<T> result = new ArrayList<>();
    for (S obj : inputs) {
      result.add(function.apply(obj));
    }
    return result;
  }
  
  /**
   * Using the iterable collection and the predicate function passed in, filters those
   * items that satisfy the predicate into a new list
   * @param inputs - iterable collection to filter
   * @param predicate - predicate function supplying the filter test
   * @return - new list of filtered items
   */
  public static <T> List<T> filter(Iterable<T> inputs, Predicate<T> predicate) {
    List<T> result = new ArrayList<>();
    for(T obj : inputs) {
      if(predicate.test(obj)) {
        result.add(obj);
      }  
    }
    return result;
  }
  
  /**
   * Using the iterable collection and the predicate function passed in, removes from the
   * given collection, those items that do not satisfy the predicate.
   * @param inputs - iterable collection to be modified
   * @param predicate - predicate function supplying the filter test
   */
  public static <T> void filterInPlace(Iterable<T> inputs, Predicate<T> predicate) {
    Iterator<T> iterator = inputs.iterator();
    while(iterator.hasNext()) {
      if(!predicate.test(iterator.next())) {
        iterator.remove();
      }
    }
  }

}
