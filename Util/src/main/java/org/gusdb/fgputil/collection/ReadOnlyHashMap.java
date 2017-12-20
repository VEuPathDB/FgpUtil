package org.gusdb.fgputil.collection;

import java.util.HashMap;

public class ReadOnlyHashMap<K,V> extends HashMap<K,V> implements ReadOnlyMap<K,V> {

  private static final long serialVersionUID = 1L;

  // implementation is handled by HashMap, but can be used as a default implementation of ReadOnlyMap

}
