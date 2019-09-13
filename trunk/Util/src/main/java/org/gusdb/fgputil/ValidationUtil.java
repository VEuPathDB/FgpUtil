package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.gusdb.fgputil.json.JsonUtil;
import org.json.JSONObject;

public class ValidationUtil {

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
