package org.gusdb.fgputil;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;

public class Named {

  private Named() {}

  public static interface NamedObject {
    public String getName();
  }

  /**
   * Utility function that converts a named object to its name.  Handy for map operations, etc.
   */
  public static final Function<NamedObject,String> TO_NAME = new Function<NamedObject,String>() {
    @Override
    public String apply(NamedObject namedObj) {
      return namedObj.getName();
    }
  };

  public static final <T extends NamedObject> Function<T, Entry<String, T>> getToMapEntry(
      @SuppressWarnings("unused") Class<T> namedClass) { // used by compiler to determine type of T
    return new Function<T, Entry<String, T>>() {
      @Override
      public Entry<String, T> apply(T obj) {
        return new TwoTuple<String, T>(obj.getName(), obj);
      }
    };
  }

  public static <T extends NamedObject> void sortByName(List<T> list, final SortDirection direction, final boolean ignoreCase) {
    Collections.sort(list, new Comparator<T>() {
      @Override public int compare(T obj1, T obj2) {
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
      }});
  }

}
