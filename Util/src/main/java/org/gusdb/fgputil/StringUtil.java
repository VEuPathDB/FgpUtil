package org.gusdb.fgputil;

import java.util.regex.Pattern;

public class StringUtil {
  private static final Pattern PAT_LINE_START = Pattern.compile("(?m)^");
  private static final int DEFAULT_INDENT = 2;

  public static String indent(final String target) {
    return indent(target, DEFAULT_INDENT);
  }

  public static String indent(final String target, final int num) {
    return num > 0
      ? PAT_LINE_START.matcher(target).replaceAll(" ".repeat(num))
      : target;
  }
}
