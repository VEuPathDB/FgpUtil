package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Convenience class for building Lists.
 * 
 * @author rdoherty
 *
 * @param <T> type contained in the enclosed List
 */
public class ListBuilder<T> {

  /**
   * Creates a typed list from a single object.  A slightly more efficient and convenient
   * function than calling:
   * 
   * new ListBuilder<T>().add(obj).toList();
   * 
   * Note that the returned list is a mutable ArrayList.
   * 
   * @param obj object to place in list
   * @return list containing passed object
   */
  public static <T> List<T> asList(T obj) {
    List<T> list = new ArrayList<>();
    list.add(obj);
    return list;
  }

  private List<T> _list;

  public ListBuilder() {
    _list = new ArrayList<T>();
  }

  public ListBuilder(List<T> list) {
    _list = list;
  }

  public ListBuilder(T[] list) {
    _list = new ArrayList<T>(Arrays.asList(list));
  }

  public ListBuilder(T obj) {
    this();
    _list.add(obj);
  }

  public ListBuilder<T> add(T obj) {
    _list.add(obj);
    return this;
  }

  public ListBuilder<T> addIf(boolean add, T obj) {
    if (add) _list.add(obj);
    return this;
  }

  public ListBuilder<T> addAll(List<T> list) {
    _list.addAll(list);
    return this;
  }

  public ListBuilder<T> addAll(T[] array) {
    _list.addAll(Arrays.asList(array));
    return this;
  }

  public List<T> toList() {
    return _list;
  }
}
