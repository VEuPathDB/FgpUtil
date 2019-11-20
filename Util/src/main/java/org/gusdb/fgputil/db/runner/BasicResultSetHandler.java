package org.gusdb.fgputil.db.runner;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;

/**
 * Collects the results of an SQL query into an in-memory data structure.
 * 
 * @author rdoherty
 */
public class BasicResultSetHandler implements ResultSetHandler<BasicResultSetHandler> {

  private List<String> _columnNames = new ArrayList<>();
  private List<Integer> _columnTypes = new ArrayList<>();
  private List<Map<String,Object>> _results = new ArrayList<>();
  
  /**
   * Reads the entire result set and its meta data into memory for later access.
   * 
   * @param rs ResultSet to be handled
   */
  @Override
  public BasicResultSetHandler handleResult(ResultSet rs) throws SQLException {
    // clear data structures in case this object is being reused
    _columnNames.clear();
    _columnTypes.clear();
    _results.clear();
    
    ResultSetMetaData meta = rs.getMetaData();
    for (int i = 1; i <= meta.getColumnCount(); i++) {
      _columnNames.add(meta.getColumnName(i));
      _columnTypes.add(meta.getColumnType(i));
    }
    while (rs.next()) {
      Map<String, Object> row = new LinkedHashMap<>();
      for (int i = 1; i <= getNumCols(); i++) {
        row.put(_columnNames.get(i-1), rs.getObject(i));
        if (rs.wasNull()) {
          // add null if DB column empty for this row; caller can determine if column name
          //  is legitimate by checking column names in this result handler
          row.put(_columnNames.get(i-1), null);
        }
      }
      _results.add(row);
    }
    return this;
  }

  /**
   * @return number of rows read from the result set
   */
  public int getNumRows() {
    return _results.size();
  }
  
  /**
   * @return number of columns returned as part of the result set
   */
  public int getNumCols() {
    return _columnNames.size();
  }
  
  /**
   * @return list of the column names returned as the result set
   */
  public List<String> getColumnNames() {
    return _columnNames;
  }

  /**
   * @return list of the data types of each column (can be compared to values
   * in {@link java.sql.Types})
   */
  public List<Integer> getColumnTypes() {
    return _columnTypes;
  }
  
  /**
   * @return row-based data structure of the results.  Columns in map will be
   * can iterated over in original query selection order.
   */
  public List<Map<String,Object>> getResults() {
    return _results;
  }
  
}
