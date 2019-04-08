package org.gusdb.fgputil.db.stream;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;

import org.gusdb.fgputil.Tuples.TwoTuple;

public class ResultSetColumnInfo extends ArrayList<TwoTuple<String, Integer>> {

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
