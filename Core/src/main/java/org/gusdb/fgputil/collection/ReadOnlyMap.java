package org.gusdb.fgputil.collection;

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

public interface ReadOnlyMap<K,V> {

  WriteableMap<K,V> toWriteableMap();

  int size();
  boolean isEmpty();
  boolean containsKey(Object key);
  Set<K> keySet();
  Collection<V> values();
  Set<Entry<K,V>> entrySet();
  V get(Object key);

}
