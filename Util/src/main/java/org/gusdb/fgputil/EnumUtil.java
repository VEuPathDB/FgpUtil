package org.gusdb.fgputil;

import java.util.Arrays;
import java.util.stream.Collectors;

import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;

public class EnumUtil {

  public static <T extends Enum<T>> boolean isValueOf(String str, Function<String, T> valueOf) {
    try { valueOf.apply(str); return true; } catch(Exception e) { return false; }
  }

  public static <T extends Enum<T>> String valuesAsString(T[] values) {
    return "[ " + Arrays.stream(values).map(Enum::name).collect(Collectors.joining(", ")) + " ]";
  }
}
