package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Named.NamedObject;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SortDirectionSpec<T extends NamedObject> {

  private static final Logger LOG = Logger.getLogger(SortDirectionSpec.class);

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

  public static <T extends NamedObject> List<SortDirectionSpec<T>> convertSorting(Map<String, Boolean> sortingAttributeMap, Map<String, T> allowedValues) {
    List<SortDirectionSpec<T>> sorting = new ArrayList<>();
    for (Entry<String, Boolean> sortingAttribute : sortingAttributeMap.entrySet()) {
      if (allowedValues.containsKey(sortingAttribute.getKey())) {
        sorting.add(new SortDirectionSpec<>(
            allowedValues.get(sortingAttribute.getKey()),
            SortDirection.getFromIsAscending(sortingAttribute.getValue())));
      }
      else {
        LOG.warn("Sort attribute [ " + sortingAttribute.getKey() + "] passed in but not found in allowed values.  Skipping...");
      }
    }
    return sorting;
  }

  public static <T extends NamedObject> Map<String, Boolean> convertSorting(List<SortDirectionSpec<T>> sorting, int maxFields) {
    Map<String, Boolean> conversion = new LinkedHashMap<>();
    int numSorts = 0;
    for (SortDirectionSpec<T> sort : sorting) {
      conversion.put(sort.getItem().getName(), sort.getDirection().isAscending());
      numSorts++;
      // don't sort by more than maximum number of fields
      if (numSorts >= maxFields) break;
    }
    return conversion;
  }
}
