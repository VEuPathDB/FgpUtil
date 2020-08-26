package org.gusdb.fgputil;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Predicate;

import org.gusdb.fgputil.Tuples.TwoTuple;

public class Named {

  private Named() {}

  public interface NamedObject {
    String getName();
    default String getFullName() {
      return getName();
    }
  }

  /**
   * Utility function that converts a named object to its name.  Handy for map operations, etc.
   */
  public static final Function<NamedObject,String> TO_NAME = NamedObject::getName;

  public static final <T extends NamedObject> Function<T, Entry<String, T>> getToMapEntry(
      @SuppressWarnings("unused") Class<T> namedClass) { // used by compiler to determine type of T
    return obj -> new TwoTuple<>(obj.getName(), obj);
  }

  public static <T extends NamedObject> void sortByName(List<T> list, final SortDirection direction, final boolean ignoreCase) {
    Collections.sort(list, (obj1, obj2) -> {
      switch(direction) {
        case DESC:
          return ignoreCase ?
              obj2.getName().compareToIgnoreCase(obj1.getName()) :
              obj2.getName().compareTo(obj1.getName());
        case ASC:
        default:
          return ignoreCase ?
              obj1.getName().compareToIgnoreCase(obj2.getName()) :
              obj1.getName().compareTo(obj2.getName());
      }
    });
  }

  public static <T extends NamedObject> Predicate<T> nameMatches(String name) {
    return named -> named.getName().equals(name);
  }
}
