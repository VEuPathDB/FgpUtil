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

  /**
   * Trims all instances of the given character from the left side of the given
   * {@code String}.
   *
   * @param target
   *   {@code String} to trim
   * @param trim
   *   {@code char} to remove
   *
   * @return the trimmed {@code String}, if the character did not appear in the
   *   given string, the returned string will be the input string.
   */
  public static String ltrim(final String target, final char trim) {
    var n = target.length();
    for (int i = 0; i < n; i++)
      if (target.charAt(i) != trim)
        return target.substring(i, n);
    return target;
  }

  /**
   * Trims all instances of the given character from the right side of the given
   * {@code String}.
   *
   * @param target
   *   {@code String} to trim
   * @param trim
   *   {@code char} to remove
   *
   * @return the trimmed {@code String}, if the character did not appear in the
   *   given string, the returned string will be the input string.
   */
  public static String rtrim(final String target, final char trim) {
    for (var i = target.length() - 1; i > -1; i--)
      if (target.charAt(i) != trim)
        return target.substring(0, i + 1);
    return target;
  }

  /**
   * Trims all instances of the given character from the left and right sides of
   * the given {@code String}.
   *
   * @param target
   *   {@code String} to trim
   * @param trim
   *   {@code char} to remove
   *
   * @return the trimmed {@code String}, if the character did not appear in the
   *   given string, the returned string will be the input string.
   */

  public static String trim(final String target, final char trim) {
    return ltrim(rtrim(target, trim), trim);
  }
}
