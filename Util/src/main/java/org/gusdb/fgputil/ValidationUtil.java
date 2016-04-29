package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONObject;

public class ValidationUtil {

  public static List<String> validateProperties(JSONObject obj, Set<String> requiredProps, Set<String> optionalProps) {
    String[] objPropNames = JSONObject.getNames(obj);
    if (objPropNames == null) objPropNames = new String[0];
    return validateProperties(Arrays.asList(objPropNames), requiredProps, optionalProps);
  }
  
  public static List<String> validateProperties(List<String> properties, Set<String> requiredProps, Set<String> optionalProps) {
    List<String> errorMsgs = new ArrayList<>();
    Set<String> foundRequiredProps = new HashSet<>();
    for (String prop : properties) {
      if (requiredProps.contains(prop)) {
        foundRequiredProps.add(prop);
      }
      else if (!optionalProps.contains(prop)) {
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
