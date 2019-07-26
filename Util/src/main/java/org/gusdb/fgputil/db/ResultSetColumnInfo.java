package org.gusdb.fgputil.db;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.gusdb.fgputil.Tuples.TwoTuple;

/**
 * Caches DB column index, name, and type from ResultSetMetaData.  Useful if
 * this information need to be retained after the providing ResultSet or
 * PreparedStatement must be closed.  Additional data may be added in the future.
 * 
 * @author rdoherty
 */
public class ResultSetColumnInfo extends ArrayList<TwoTuple<String, Integer>> {

  private static final long serialVersionUID = 1L;

  public ResultSetColumnInfo(ResultSet rs) throws SQLException {
    init(rs.getMetaData());
  }

  public ResultSetColumnInfo(PreparedStatement rs) throws SQLException {
    init(rs.getMetaData());
  }

  private void init(ResultSetMetaData metaData) throws SQLException {
    for (int i = 1; i <= metaData.getColumnCount(); i++) {
      add(new TwoTuple<>(metaData.getColumnLabel(i), metaData.getColumnType(i)));
    }
  }

  public int getColumnCount() {
    return size();
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
