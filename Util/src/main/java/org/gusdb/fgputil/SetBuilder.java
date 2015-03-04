package org.gusdb.fgputil;

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

  public SetBuilder<T> addAll(Set<T> set) {
    _set.addAll(set);
    return this;
  }

  public Set<T> toSet() {
    return _set;
  }
}
