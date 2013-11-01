package org.gusdb.fgputil;

public class FormatUtil {
	
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
    StringBuilder sb = new StringBuilder("[ ");
    if (array.length > 0) {
      sb.append(array[0].toString());
    }
    for (int i = 1; i < array.length; i++) {
      sb.append(", ").append(array[i].toString());
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
}
