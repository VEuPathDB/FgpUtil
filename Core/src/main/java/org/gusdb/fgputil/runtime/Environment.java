package org.gusdb.fgputil.runtime;

import java.util.Optional;

public class Environment {

  public static String getOptionalVar(String name, String defaultValue) {
    return Optional.ofNullable(System.getenv(name)).orElse(defaultValue);
  }

  public static String getRequiredVar(String name) {
    String value = System.getenv(name);
    if (value == null) {
      throw new RuntimeException("Required environment variable '" + name + "' is not defined.");
    }
    return value;
  }
}
