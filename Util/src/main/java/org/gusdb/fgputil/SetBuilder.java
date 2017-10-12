package org.gusdb.fgputil;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Convenience class for building Sets
 * 
 * @author rdoherty
 * @param <T> type contained in the enclosed Set
 */
public class SetBuilder<T> {

  private Set<T> _set;

  public SetBuilder() {
    _set = new HashSet<T>();
  }

  public SetBuilder(Set<T> set) {
    _set = set;
  }

  public SetBuilder(T obj) {
    this();
    _set.add(obj);
  }

  public SetBuilder<T> add(T obj) {
    _set.add(obj);
    return this;
  }

  public SetBuilder<T> addIf(boolean add, T obj) {
    if (add) _set.add(obj);
    return this;
  }

  public SetBuilder<T> addAll(Collection<? extends T> set) {
    _set.addAll(set);
    return this;
  }

  /**
   * Tries to find oldObj in the set; if present, removes oldObj and adds newObj.
   * If not found, does nothing (i.e. newObj is not added).
   * 
   * @param oldObj object to be replaced
   * @param newObj object to replace oldObj with
   * @return this set builder
   */
  public SetBuilder<T> replace(T oldObj, T newObj) {
    if (_set.remove(oldObj)) {
      _set.add(newObj);
    }
    return this;
  }

  public Set<T> toSet() {
    return _set;
  }
}
