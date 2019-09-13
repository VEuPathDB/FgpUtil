package org.gusdb.fgputil.collection;

import java.util.HashMap;
import java.util.Map;

public class WriteableHashMap<K,V> extends HashMap<K,V> implements WriteableMap<K,V> {

  private static final long serialVersionUID = 1L;

  public WriteableHashMap() {
    super();
  }

  public WriteableHashMap(Map<K,V> map) {
    super(map);
  }

  @Override
  public Map<K, V> getUnderlyingMap() {
    return this;
  }

  @Override
  public WriteableMap<K, V> toWriteableMap() {
    return this;
  }

}
