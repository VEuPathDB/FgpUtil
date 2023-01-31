package org.gusdb.fgputil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.collection.FixedSizeStringMap;

/**
 * Facilitates conversion of lines of delimited data into named columns.  The
 * column names are specified via a header line or explicit list and cannot be
 * modified.
 *
 * @author rdoherty
 */
public class DelimitedDataParser {

  private final FixedSizeStringMap.Builder _lineBuilder;
  private final String _delimiterRegex;
  private final boolean _enforceStrictColumns;

  public DelimitedDataParser(List<String> columnNames, String delimiterRegex, boolean enforceStrictColumns) {
    _delimiterRegex = delimiterRegex;
    _enforceStrictColumns = enforceStrictColumns;
    _lineBuilder = new FixedSizeStringMap.Builder(columnNames.toArray(new String[columnNames.size()]));
  }

  public DelimitedDataParser(String headerLine, String delimiterRegex, boolean enforceStrictColumns) {
    _delimiterRegex = delimiterRegex;
    _enforceStrictColumns = enforceStrictColumns;
    _lineBuilder = new FixedSizeStringMap.Builder(headerLine.split(delimiterRegex));
  }

  public List<String> getColumnNames() {
    return new ArrayList<>(_lineBuilder.keySet());
  }

  public Optional<Integer> indexOfColumn(String columnName) {
    int index = getColumnNames().indexOf(columnName);
    return index == -1 ? Optional.empty() : Optional.of(index);
  }

  public String[] parseLineToArray(String line) {
    String[] values = line.split(_delimiterRegex, -1);
    if (_enforceStrictColumns && values.length != _lineBuilder.size()) {
      throw new RuntimeException("Expected " + _lineBuilder.size() + " columns but parsed " + values.length + " in line:\n" + line);
    }
    return values;
  }

  public Map<String,String> parseLine(String line) {
    return _lineBuilder.build().putAll(parseLineToArray(line));
  }
}
