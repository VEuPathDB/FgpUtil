package org.gusdb.fgputil;

import java.util.HashMap;
import java.util.Map;

/**
 * Convenience class for building Maps.
 * 
 * @author rdoherty
 *
 * @param <S> key type of the enclosed Map
 * @param <T> value type of the enclosed Map
 */
public class MapBuilder<S,T> {

  private Map<S,T> _map;
  
  public MapBuilder() {
    _map = new HashMap<>();
  }
  
  public MapBuilder(Map<S,T> map) {
    _map = map;
  }
  
  public MapBuilder(S first, T second) {
    this();
    _map.put(first, second);
  }
  
  public MapBuilder<S,T> put(S first, T second) {
    _map.put(first, second);
    return this;
  }
  
  public MapBuilder<S,T> putAll(Map<S,T> map) {
    _map.putAll(map);
    return this;
  }
  
  public Map<S,T> toMap() {
    return _map;
  }
}
