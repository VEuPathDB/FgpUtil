package org.gusdb.fgputil;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * Facilitates conversion of lines of delimited data into named columns.  The
 * column names are specified via a header line or explicit list and cannot be
 * modified.
 *
 * @author rdoherty
 */
public class DelimitedDataParser {

  private final String[] _columnNames;
  private final String _delimiterRegex;
  private final boolean _enforceStrictColumns;

  public DelimitedDataParser(List<String> columnNames, String delimiterRegex, boolean enforceStrictColumns) {
    _delimiterRegex = delimiterRegex;
    _enforceStrictColumns = enforceStrictColumns;
    _columnNames = columnNames.toArray(new String[columnNames.size()]);
  }

  public DelimitedDataParser(String headerLine, String delimiterRegex, boolean enforceStrictColumns) {
    _delimiterRegex = delimiterRegex;
    _enforceStrictColumns = enforceStrictColumns;
    _columnNames = headerLine.split(delimiterRegex);
  }

  public List<String> getColumnNames() {
    return Collections.unmodifiableList(Arrays.asList(_columnNames));
  }

  public Optional<Integer> indexOfColumn(String columnName) {
    int index = getColumnNames().indexOf(columnName);
    return index == -1 ? Optional.empty() : Optional.of(index);
  }

  public String[] parseLineToArray(String line) {
    String[] values = line.split(_delimiterRegex);
    if (values.length == _columnNames.length - 1) {
      // Impossible to tell using split() if a trailing delimiter was present;
      //   for now, assume it was and add an empty string so col count matches.
      // Possible upgrade for the future to make sure trailing delimiter present
      values = ArrayUtil.concatenate(values, new String[] { "" });
    }
    if (_enforceStrictColumns && values.length != _columnNames.length) {
      throw new RuntimeException("Expected " + _columnNames.length + " columns but parsed " + values.length + " in line:\n" + line);
    }
    return values;
  }

  public LinkedHashMap<String,String> parseLine(String line) {
    String[] values = parseLineToArray(line);
    LinkedHashMap<String,String> map = new LinkedHashMap<>();
    int numToParse = Math.min(values.length, _columnNames.length);
    for (int i = 0; i < numToParse; i++) {
      map.put(_columnNames[i], values[i]);
    }
    return map;
  }
}
