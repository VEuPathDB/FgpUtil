package org.gusdb.fgputil.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.gusdb.fgputil.Tuples.ThreeTuple;

/**
 * Caches DB column index, name, and type from ResultSetMetaData.  Useful if
 * this information need to be retained after the providing ResultSet or
 * PreparedStatement must be closed.  Additional data may be added in the future.
 * 
 * @author rdoherty
 */
public class ResultSetColumnInfo extends ArrayList<ThreeTuple<String, Integer, Integer>> {

  public ResultSetColumnInfo(ResultSet rs) throws SQLException {
    init(rs.getMetaData());
  }

  public ResultSetColumnInfo(PreparedStatement rs) throws SQLException {
    init(rs.getMetaData());
  }

  private void init(ResultSetMetaData metaData) throws SQLException {
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      add(new ThreeTuple<>(metaData.getColumnLabel(i), metaData.getColumnType(i), metaData.getPrecision(i)));
    }
  }

  public int getColumnCount() {
    return size();
  }

  /**
   * @param columnIndex index of column for which size is needed
   * @return estimated size in bytes of the column referred to by the passed index
   */
  public int getEstimatedColumnSize(int columnIndex) {
    switch(DbColumnType.getFromSqlType(getColumnType(columnIndex))) {
      case BINARY_DATA:
        return getPrecision(columnIndex); // number of bytes of binary data
      case BOOLEAN:
        return 1; // booleans are little
      case CLOB:
        return 1; // CLOBs will likely be streamed separately
      case DATE_TIME:
        return getPrecision(columnIndex) * 2; // number of chars of string representation
      case DOUBLE:
        return 8; // eight bytes per double
      case LONG_INT:
        return 8; // eight bytes per long; assume value fits in long
      case OTHER:
        return 1; // ??
      case STRING:
        return getPrecision(columnIndex) * 2; // number of chars allowed in string
      default:
        return 1; // ??
    }
  }

  public int getEstimatedRowSize() {
    return stream().map(col -> col.getThird()).mapToInt(Integer::intValue).sum();
  }

  private int getPrecision(int columnIndex) {
    return get(columnIndex - 1).getThird();
  }

  public int getColumnType(int columnIndex) {
    return get(columnIndex - 1).getSecond();
  }

  public int getColumnType(String columnLabel) {
    return stream()
        .filter(tuple -> tuple.getFirst().equals(columnLabel))
        .findFirst()
        .orElseThrow(() -> new IllegalArgumentException("Column " + columnLabel + " is not present."))
        .getSecond();
  }

  public String getColumnLabel(int columnIndex) {
    return get(columnIndex-1).getFirst();
  }

  public int getColumnIndex(String columnLabel) {
    for (int i = 0; i < size(); i++) {
      if (get(i).getFirst().equals(columnLabel)) {
        return i + 1;
      }
    }
    throw new IllegalArgumentException("Column " + columnLabel + " is not present.");
  }

}
