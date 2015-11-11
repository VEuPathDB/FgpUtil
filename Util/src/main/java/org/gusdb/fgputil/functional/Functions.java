package org.gusdb.fgputil.functional;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;

public class Functions {

  public static <S,T> List<T> mapToList(Iterable<S> inputs, Function<S,T> function) {
    List<T> result = new ArrayList<>();
    for (S obj : inputs) {
      result.add(function.apply(obj));
    }
    return result;
  }
}
