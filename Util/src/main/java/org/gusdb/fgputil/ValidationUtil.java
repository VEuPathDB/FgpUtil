package org.gusdb.fgputil;

import java.util.*;
import java.util.function.Supplier;

import org.gusdb.fgputil.json.JsonUtil;
import org.json.JSONObject;

public class ValidationUtil {

  /**
   * Validate that the given input value is no longer than the given max length.
   * Throws the provided exception if this validation fails.
   *
   * @param val value to check
   * @param len max length for value
   * @param err error to throw if value is longer than len
   * @param <E> type of exception that will be thrown
   *
   * @return The given value if it passes validation
   *
   * @throws E if the value is longer than the given max length
   * @throws NullPointerException if the given value is null or if the supplier
   * is null.
   */
  public static <E extends Throwable> String maxLength(String val, int len,
      Supplier<E> err) throws E {
    Objects.requireNonNull(val);
    Objects.requireNonNull(err);

    if(val.length() > len)
      throw err.get();
    return val;
  }

  public static List<String> validateProperties(JSONObject obj, Set<String> requiredProps, Set<String> optionalProps) {
    return validateProperties(obj, requiredProps, optionalProps, false);
  }

  public static List<String> validateProperties(Set<String> properties, Set<String> requiredProps, Set<String> optionalProps) {
    return validateProperties(properties, requiredProps, optionalProps, false);
  }

  public static List<String> validateProperties(JSONObject obj, Set<String> requiredProps, Set<String> optionalProps, boolean allowSupplementalProperties) {
    return validateProperties(JsonUtil.getKeys(obj), requiredProps, optionalProps, allowSupplementalProperties);
  }

  public static List<String> validateProperties(Set<String> properties, Set<String> requiredProps, Set<String> optionalProps,  boolean allowSupplementalProperties) {
    List<String> errorMsgs = new ArrayList<>();
    Set<String> foundRequiredProps = new HashSet<>();
    for (String prop : properties) {
      if (requiredProps.contains(prop)) {
        foundRequiredProps.add(prop);
      }
      else if (!allowSupplementalProperties && !optionalProps.contains(prop)) {
        errorMsgs.add("Unrecognized property: '" + prop + "'.");
      }
    }
    for (String requiredProp : requiredProps) {
      if (!foundRequiredProps.contains(requiredProp)) {
        errorMsgs.add("Property '" + requiredProp + "' is required but missing.");
      }
    }
    return errorMsgs;
  }
}
