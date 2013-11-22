package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.List;

/**
 * Convenience class for building Lists.
 * 
 * @author rdoherty
 *
 * @param <T> type contained in the enclosed List
 */
public class ListBuilder<T> {

  private List<T> _list;
  
  public ListBuilder() {
    _list = new ArrayList<T>();
  }
  
  public ListBuilder(List<T> list) {
    _list = list;
  }
  
  public ListBuilder(T obj) {
    this();
    _list.add(obj);
  }
  
  public ListBuilder<T> add(T obj) {
    _list.add(obj);
    return this;
  }
  
  public ListBuilder<T> addAll(List<T> list) {
    _list.addAll(list);
    return this;
  }
  
  public List<T> toList() {
    return _list;
  }
}
