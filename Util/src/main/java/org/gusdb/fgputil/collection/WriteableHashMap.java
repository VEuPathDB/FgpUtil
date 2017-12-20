package org.gusdb.fgputil.collection;

import java.util.HashMap;
import java.util.Map;

public class WriteableHashMap<K,V> extends HashMap<K,V> implements WriteableMap<K,V> {

  private static final long serialVersionUID = 1L;

  @Override
  public Map<K, V> getUnderlyingMap() {
    return this;
  }

}
