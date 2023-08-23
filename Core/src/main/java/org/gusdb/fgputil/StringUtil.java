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

  /**
   * Tests whether the given string is in the valid UUID V4 format.
   *
   * @param s String to test.
   *
   * @return {@code true} if the given string is a valid UUID V4 string,
   * otherwise {@code false}.
   */
  // XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX
  public static boolean isUuidV4(String s) {
    if (s.length() != 36)
      return false;

    // validate the first 8 digits
    for (int i = 0; i < 8; i++)
      if (!isHexDigit(s.charAt(i)))
        return false;

    // first dash
    if (s.charAt(8) != '-')
      return false;

    // first block of 4 digits
    for (int i = 9; i < 13; i++)
      if (!isHexDigit(s.charAt(i)))
        return false;

    // second dash
    if (s.charAt(13) != '-')
      return false;

    // second block of 4 digits
    for (int i = 14; i < 18; i++)
      if (!isHexDigit(s.charAt(i)))
        return false;

    // third dash
    if (s.charAt(18) != '-')
      return false;

    // last block of 4 digits
    for (int i = 19; i < 23; i++)
      if (!isHexDigit(s.charAt(i)))
        return false;

    // last dash
    if (s.charAt(23) != '-')
      return false;

    // last block of digits (12 digits)
    for (int i = 24; i < 36; i++)
      if (!isHexDigit(s.charAt(i)))
        return false;

    return true;
  }

  /**
   * Tests whether the given character is a valid hexadecimal digit.
   *
   * @param c Character to test.
   *
   * @return {@code true} if the given character is a valid hex digit, otherwise
   * {@code false}.
   */
  public static boolean isHexDigit(char c) {
    return (c >= '0' && c <= '9')
      || (c >= 'A' && c <= 'F')
      || (c >= 'a' && c <= 'f');
  }
}
