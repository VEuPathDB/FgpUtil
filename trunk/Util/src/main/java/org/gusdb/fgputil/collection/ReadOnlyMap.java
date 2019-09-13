package org.gusdb.fgputil.collection;

import java.util.Collection;
import java.util.Set;
import java.util.Map.Entry;

public interface ReadOnlyMap<K,V> {

  public WriteableMap<K,V> toWriteableMap();

  public int size();
  public boolean isEmpty();
  public boolean containsKey(Object key);
  public Set<K> keySet();
  public Collection<V> values();
  public Set<Entry<K,V>> entrySet();
  public V get(Object key);

}
