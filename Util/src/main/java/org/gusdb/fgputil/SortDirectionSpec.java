package org.gusdb.fgputil;

import org.gusdb.fgputil.Named.NamedObject;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class SortDirectionSpec<T extends NamedObject> {
  T _item;
  SortDirection _direction;
  
  public SortDirectionSpec(T item, SortDirection direction) {
    _item = item;
    _direction = direction;
  }

  @JsonIgnore
  public T getItem() { return _item; }

  public String getItemName() { return _item.getName(); }

  public SortDirection getDirection() { return _direction; }

}
