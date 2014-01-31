package org.gusdb.fgputil;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.util.Map;
import java.util.Map.Entry;

public class FormatUtil {

  public static final String NL = System.getProperty("line.separator");
  
  private FormatUtil() {}
  
  public static String getStackTrace(Throwable t) {
    StringWriter str = new StringWriter(150);
    t.printStackTrace(new PrintWriter(str));
    return str.toString();
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

  public static String arrayToString(Object[] array) {
    return arrayToString(array, ", ");
  }

  public static String arrayToString(Object[] array, String delim) {
    StringBuilder sb = new StringBuilder("[ ");
    if (array.length > 0) {
      sb.append(array[0].toString());
    }
    for (int i = 1; i < array.length; i++) {
      sb.append(delim).append(array[i].toString());
    }
    return sb.append(" ]").toString();
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

  public static enum Style {
    SINGLE_LINE(" ", "", ", ", ""),
    MULTI_LINE(NL, "   ", ","+NL, NL);
    
    public final String introDelimiter;
    public final String recordIndent;
    public final String mapArrow = " => ";
    public final String recordDelimiter;
    public final String endDelimiter;
    
    private Style(String id, String ri, String rd, String ed) {
      introDelimiter = id; recordIndent = ri;
      recordDelimiter = rd; endDelimiter = ed;
    }
  }
  
  public static <S,T> String prettyPrint(Map<S,T> map) {
    return prettyPrint(map, Style.SINGLE_LINE);
  }
  
  public static <S,T> String prettyPrint(Map<S,T> map, Style style) {
    StringBuilder sb = new StringBuilder("{").append(style.introDelimiter);
    boolean firstRecord = true;
    for (Entry<S,T> entry : map.entrySet()) {
      sb.append(firstRecord ? "" : style.recordDelimiter).append(style.recordIndent)
        .append(entry.getKey().toString()).append(style.mapArrow)
        .append(entry.getValue() == null ? null : entry.getValue().toString());
      firstRecord = false;
    }
    return sb.append(style.endDelimiter).append("}")
             .append(style.endDelimiter).toString();
  }

  public static String getPctFromRatio(long numerator, long denominator) {
    Double ratio = (double)numerator / (double)denominator;
    return new DecimalFormat("##0.0").format(ratio * 100D) + "%";
  }
}
