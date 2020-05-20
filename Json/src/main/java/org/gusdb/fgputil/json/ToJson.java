package org.gusdb.fgputil.json;

import static org.gusdb.fgputil.functional.Functions.reduce;

import java.util.Map;

import org.json.JSONObject;

/**
 * Tells observers that this class can be converted to a JSONObject and provides
 * a method to do so.  Also contains static functions that act on implementing
 * classes.
 *
 * @author rdoherty
 */
public interface ToJson {

  public static <T,S extends ToJson> JSONObject mapToJson(Map<T,S> map) {
    return reduce(
      map.entrySet(),
      (acc, next) -> acc.put(next.getKey().toString(), next.getValue().toJson()),
      new JSONObject());
  }

  public JSONObject toJson();

}
