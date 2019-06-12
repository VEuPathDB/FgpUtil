package org.gusdb.fgputil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.gusdb.fgputil.functional.Functions;

public class FormatUtil {

  public static final String NL = System.lineSeparator();
  public static final String TAB = "\t";
  public static final String UTF8_ENCODING = "UTF-8";

  public static final DateTimeFormatter STANDARD_DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
  public static final DateTimeFormatter STANDARD_DATE_TIME_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

  public interface MultiLineToString {
    public String toMultiLineString(String indentation);
  }

  private FormatUtil() {}

  private static class CurrentStackTrace extends Throwable { }

  public static String getCurrentStackTrace() {
    return getStackTrace(new CurrentStackTrace());
  }

  public static String getStackTrace(Throwable t) {
    StringWriter str = new StringWriter(150);
    t.printStackTrace(new PrintWriter(str));
    return str.toString();
  }

  public static Date parseDate(String date) throws DateTimeParseException {
    return Date.from(LocalDate
        .parse(date, STANDARD_DATE_FORMAT)
        .atStartOfDay()
        .atZone(ZoneId.systemDefault())
        .toInstant());
  }

  public static Date parseDateTime(String dateTime) throws DateTimeParseException {
    return Date.from(LocalDateTime
        .parse(dateTime, STANDARD_DATE_TIME_FORMAT)
        .atZone(ZoneId.systemDefault())
        .toInstant());
  }

  public static String formatDate(Date date) {
    return STANDARD_DATE_FORMAT.format(date
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDate());
  }

  public static String formatDateTime(Date date) {
    return STANDARD_DATE_TIME_FORMAT.format(date
        .toInstant()
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime());
  }

  public static String shrinkUtf8String(String str, int maxBytes) {
    // assume incoming encoding uses >= 1 byte per char and cut to maxBytes chars
    if (str.length() > maxBytes) {
      str = str.substring(0, maxBytes);
    }
    // cut chars one-at-a-time until small enough to fit (TODO: use binary search?)
    while (FormatUtil.getUtf8EncodedBytes(str).length > maxBytes) {
      str = str.substring(0, str.length()-1);
    }
    return str;
  }

  public static byte[] getUtf8EncodedBytes(String s) {
    try {
      return s.getBytes(UTF8_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      // this should never happen; if it does, wrap in RuntimeException
      throw new RuntimeException(UTF8_ENCODING + " encoding no longer supported by Java.", e);
    }
  }

  public static String decodeUtf8EncodedBytes(byte[] bytes) {
    try {
      return new String(bytes, UTF8_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      // this should never happen; if it does, wrap in RuntimeException
      throw new RuntimeException(UTF8_ENCODING + " encoding no longer supported by Java.", e);
    }
  }

  public static String urlEncodeUtf8(String s) {
    try {
      if (s == null) return null;
      return URLEncoder.encode(s, UTF8_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      // this should never happen; if it does, wrap in RuntimeException
      throw new RuntimeException(UTF8_ENCODING + " encoding no longer supported by Java.", e);
    }
  }

  public static String urlDecodeUtf8(String s) {
    try {
      if (s == null) return null;
      return URLDecoder.decode(s, UTF8_ENCODING);
    }
    catch (UnsupportedEncodingException e) {
      // this should never happen; if it does, wrap in RuntimeException
      throw new RuntimeException(UTF8_ENCODING + " encoding no longer supported by Java.", e);
    }
  }

  /**
   * Attempts to convert the given text into HTML, replacing special characters
   * with their HTML equivalents.
   * TODO: this method should be improved!
   *
   * @param str string to convert
   * @return converted string
   */
  public static String escapeHtml(String str) {
    return str
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll("&", "&amp;")
      .replaceAll("\n", "<br/>\n");
  }

  public static String splitCamelCase(String s) {
    return s.replaceAll(
      String.format("%s|%s|%s",
        "(?<=[A-Z])(?=[A-Z][a-z])",
        "(?<=[^A-Z])(?=[A-Z])",
        "(?<=[A-Za-z])(?=[^A-Za-z])"),
      " ");
  }

  public static String multiLineFormat(String str, int maxCharsPerLine) {
    String[] tokens = str.split(" ");
    String newS = "";
    int lineTotal = 0;
    for (int curTok = 0; curTok < tokens.length; curTok++) {
      if (newS.equals("") || // never add newline before first token
          lineTotal + 1 + tokens[curTok].length() <= maxCharsPerLine ||
          lineTotal == 0 && tokens[curTok].length() > maxCharsPerLine) {
        // add this token to the current line
      }
      else {
        // start new line
        newS += "\\n";
        lineTotal = 0;
      }
      lineTotal += (lineTotal == 0 ? 0 : 1) + tokens[curTok].length();
      newS += (lineTotal == 0 ? "" : " ") + tokens[curTok];
    }
    return newS;
  }

  public static <T> String join(Iterable<T> iterable, String delim) {
    StringBuilder sb = new StringBuilder();
    boolean first = true;
    for (T item : iterable) {
      if (!first) sb.append(delim);
      sb.append(item);
      first = false;
    }
    return sb.toString();
  }

  public static <T> String join(Stream<T> stream, String delim) {
    return join((Iterable<T>)stream::iterator, delim);
  }

  public static String join(Object[] array, String delim) {
    if (array == null || array.length == 0) return "";
    StringBuilder sb = new StringBuilder();
    sb.append(array[0] == null ? "null" : array[0].toString());
    for (int i = 1; i < array.length; i++) {
      sb.append(delim).append(array[i] == null ? "null" : array[i].toString());
    }
    return sb.toString();
  }

  public static String arrayToString(Object[] array) {
    return arrayToString(array, ", ");
  }

  public static String arrayToString(Object[] array, String delim) {
    if (array == null) return "null";
    StringBuilder sb = new StringBuilder("[ ");
    sb.append(join(array, delim));
    return sb.append(" ]").toString();
  }

  public static String printArray(String[] array) {
    StringBuilder sb = new StringBuilder();
    sb.append("{");
    for (String s : array) {
        if (sb.length() > 1) sb.append(", ");
        sb.append("\"" + s + "\"");
    }
    sb.append("}");
    return sb.toString();
  }

  public static String printArray(String[][] array) {
    StringBuilder sb = new StringBuilder();
    for (String[] parts : array)
      sb.append(printArray(parts)).append(NL);
    return sb.toString();
  }

  public static String getCamelCaseDisplayVal(String str) {
    StringBuilder newStr = new StringBuilder();
    boolean justSawSpace = true; // set so first char is upper case
    str = str.trim();
    for (int i=0; i < str.length(); i++) {
      char thisChar = str.charAt(i);
      if (thisChar == ' ' || thisChar == '_' || thisChar == '-') {
        if (!justSawSpace) { // only do a single whitespace char
          newStr.append(' ');
          justSawSpace = true;
        }
      } else if (justSawSpace) {
        newStr.append(String.valueOf(thisChar).toUpperCase());
        justSawSpace = false;
      } else {
        newStr.append(String.valueOf(thisChar).toLowerCase());
      }
    }
    return newStr.toString();
  }

  public static boolean isInteger(String s) {
    try { Integer.parseInt(s); return true; }
    catch (NumberFormatException e) { return false; }
  }

  public enum Style {
    SINGLE_LINE(" ", "", ", ", " "),
    MULTI_LINE(NL, "   ", ","+NL, NL);

    public final String introDelimiter;
    public final String recordIndent;
    public final String mapArrow = " => ";
    public final String recordDelimiter;
    public final String endDelimiter;

    Style(String id, String ri, String rd, String ed) {
      introDelimiter = id; recordIndent = ri;
      recordDelimiter = rd; endDelimiter = ed;
    }
  }

  /**
   * Returns a "pretty" string representation of the passed map using
   * <code>Style.SINGLE_LINE</code>.
   *
   * @param map map to print
   * @return pretty string value of map
   */
  public static <S,T> String prettyPrint(Map<S,T> map) {
    return prettyPrint(map, Style.SINGLE_LINE);
  }

  /**
   * Returns a "pretty" string representation of the passed map using
   * the passed format style and the value's toString() method.
   *
   * @param map map to print
   * @return pretty string value of map
   */
  public static <S,T> String prettyPrint(Map<S,T> map, Style style) {
    return prettyPrint(map, style, new Functions.ToStringFunction<T>());
  }

  /**
   * Returns a "pretty" string representation of the passed map using
   * the passed format style.
   *
   * @param map map to print
   * @param style formatting style
   * @param toString function to convert the map values to strings
   * @return pretty string value of map
   */
  public static <S,T> String prettyPrint(Map<S,T> map, Style style, Function<T,String> toString) {
    if (map == null) return "null";
    StringBuilder sb = new StringBuilder("{").append(style.introDelimiter);
    boolean firstRecord = true;
    for (Entry<S,T> entry : map.entrySet()) {
      sb.append(firstRecord ? "" : style.recordDelimiter).append(style.recordIndent)
        .append(entry.getKey().toString()).append(style.mapArrow)
        .append(entry.getValue() == null ? null : toString.apply(entry.getValue()));
      firstRecord = false;
    }
    return sb.append(style.endDelimiter).append("}")
             .append(style.endDelimiter).toString();
  }

  public static String getPctFromRatio(long numerator, long denominator) {
    Double ratio = (double)numerator / (double)denominator;
    return new DecimalFormat("##0.0").format(ratio * 100D) + "%";
  }

  public static String paramsToString(Map<String, String[]> parameters) {
    StringBuilder str = new StringBuilder("{" + NL);
    for (Entry<String, String[]> param : parameters.entrySet()) {
      str.append("   ").append(param.getKey()).append(": ").append(arrayToString(param.getValue())).append(NL);
    }
    return str.append("}").append(NL).toString();
  }

 /* Algorithm for the method below
  *
  * if lower or underscore
  *   print this
  * else (upper)
  *   if prev underscore, print lc(this)
  *   else if prev lower or (not end and (next lower or underscore)), print _ + lc(this)
  *   else print lc(this)
  */

  private static List<Character> UPPER_CASE_LETTERS = ArrayUtil.asList("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
  private static boolean isUnderscore(char c) { return c == '_'; }
  private static boolean isLower(char c) { return !isUnderscore(c) && !UPPER_CASE_LETTERS.contains(c); }

  // FIXME: Yes, this is slow.  Someone with more time should rewrite with regexes or at least ASCII nums
  public static String toUnderscoreFormat(String camelCaseName) {
    StringBuilder builder = new StringBuilder(camelCaseName.substring(0,1).toLowerCase());
    for (int i = 1; i < camelCaseName.length(); i++) {
      String thisCharStr = camelCaseName.substring(i,i+1);
      char thisChar = thisCharStr.charAt(0);
      char previousChar = camelCaseName.charAt(i-1);
      if (isUnderscore(thisChar) || isLower(thisChar)) {
        builder.append(thisChar);
      }
      else { // upper-case letter
        if (isUnderscore(previousChar)) {
          builder.append(thisCharStr.toLowerCase());
        }
        else if (isLower(previousChar) || (i + 1 != camelCaseName.length() &&
            (isUnderscore(camelCaseName.charAt(i+1)) || isLower(camelCaseName.charAt(i+1))))) {
          builder.append('_').append(thisCharStr.toLowerCase());
        }
        else {
          builder.append(thisCharStr.toLowerCase());
        }
      }
    }
    return builder.toString();
  }

  /**
   * Log4j only accepts logger names using dot delimiters, but Class.getName()
   * returns "package.InnerClass$OuterClass", which is not referenceable by the
   * name attribute of a logger tag in log4j.xml.  This function gives a name
   * usable by both.
   *
   * @param clazz
   *   inner class name
   *
   * @return the "code-style" inner class name
   */
  public static String getInnerClassLog4jName(Class<?> clazz) {
    return clazz.getName().replace("$", ".");
  }

  /**
   * @param values
   *   an array of enum values
   *
   * @return readable array-style display of the values
   */
  public static <T extends Enum<T>> String enumValuesAsString(T[] values) {
    return "[ " + Arrays.stream(values).map(Enum::name).collect(Collectors.joining(", ")) + " ]";
  }
}
