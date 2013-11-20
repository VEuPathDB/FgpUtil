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

  private List<T> _myList;
  
  public ListBuilder() {
    _myList = new ArrayList<T>();
  }
  
  public ListBuilder(T obj) {
    this();
    _myList.add(obj);
  }
  
  public <L extends List<T>> ListBuilder(L list) {
    _myList = list;
  }
  
  public ListBuilder<T> add(T obj) {
    _myList.add(obj);
    return this;
  }
  
  public ListBuilder<T> add(List<T> list) {
    _myList.addAll(list);
    return this;
  }
  
  public List<T> toList() {
    return _myList;
  }
}
