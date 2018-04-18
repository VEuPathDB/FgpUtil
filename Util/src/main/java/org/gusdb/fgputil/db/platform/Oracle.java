/**
 * 
 */
package org.gusdb.fgputil.db.platform;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunner.ResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunnerException;

/**
 * @author Jerric Gao
 */
public class Oracle extends DBPlatform {

  private static final Logger LOG = Logger.getLogger(Oracle.class);

  public Oracle() {
    super();
  }
  
  @Override
  public String getNvlFunctionName() {
	  return "NVL";
  }
  
  @Override
  public String getDriverClassName() {
    return "oracle.jdbc.driver.OracleDriver";
  }

  @Override
  public String getValidationQuery() {
    return "SELECT 'ok' FROM dual";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#createSequence(java.lang.String, int, int)
   */
  @Override
  public void createSequence(DataSource dataSource, String sequence, int start, int increment)
      throws SQLException {
    StringBuffer sql = new StringBuffer("CREATE SEQUENCE ");
    sql.append(sequence);
    sql.append(" START WITH ").append(start);
    sql.append(" INCREMENT BY ").append(increment);
    SqlUtils.executeUpdate(dataSource, sql.toString(), "wdk-create-sequence");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getBooleanDataType()
   */
  @Override
  public String getBooleanDataType() {
    return "NUMBER(1)";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getNumberDataType(int)
   */
  @Override
  public String getNumberDataType(int size) {
    return "NUMBER(" + size + ")";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getStringDataType(int)
   */
  @Override
  public String getStringDataType(int size) {
    return "VARCHAR(" + size + ")";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getClobDataType()
   */
  @Override
  public String getClobDataType() {
    return "CLOB";
  }

  @Override
  public String getBlobDataType() {
    return "BLOB";
  }

  @Override
  public int getBlobSqlType() {
    return Types.BLOB;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getMinusOperator()
   */
  @Override
  public String getMinusOperator() {
    return "MINUS";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getNextId(java.lang.String, java.lang.String)
   */
  @Override
  public long getNextId(DataSource dataSource, String schema, String table) throws SQLException {
    schema = normalizeSchema(schema);
    StringBuffer sql = new StringBuffer("SELECT ");
    sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
    sql.append(".nextval FROM dual");
    BigDecimal id = (BigDecimal) SqlUtils.executeScalar(dataSource, sql.toString(), "select-next-id");
    return id.longValue();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getNextIdSqlExpression(java.lang. String, java.lang.String)
   */
  @Override
  public String getNextIdSqlExpression(String schema, String table) {
    schema = normalizeSchema(schema);

    StringBuilder sql = new StringBuilder("");
    sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
    sql.append(".nextval");
    return sql.toString();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getClobData(java.sql.ResultSet, java.lang.String)
   */
  @Override
  public String getClobData(ResultSet rs, String columnName) throws SQLException {
    Clob messageClob = rs.getClob(columnName);
    if (messageClob == null)
      return null;
    return messageClob.getSubString(1, (int) messageClob.length());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getPagedSql(java.lang.String, int, int)
   */
  @Override
  public String getPagedSql(String sql, int startIndex, int endIndex, boolean includeRowIndex) {

    String rowIndex = includeRowIndex? ", " + getRowNumberColumn() + " as row_index " : "";

    StringBuffer buffer = new StringBuffer();
    // construct the outer query
    buffer.append("SELECT lb.*" + rowIndex + " FROM (");
    // construct the inner nested query
    buffer.append("SELECT ub.*, rownum AS row_index FROM (");
    buffer.append(sql);
    buffer.append(") ub");
    if (endIndex > -1) {
      buffer.append(" WHERE rownum <= ").append(endIndex);
    }
    buffer.append(") lb WHERE lb.row_index >= ").append(startIndex);
    return buffer.toString();
  }

  @Override
  public boolean checkTableExists(DataSource dataSource, String schema, String tableName)
      throws SQLException, DBStateException {
    StringBuilder sql = new StringBuilder("SELECT count(*) FROM ALL_TABLES ");
    sql.append("WHERE table_name = '");
    sql.append(tableName.toUpperCase()).append("'");
    if (schema.charAt(schema.length() - 1) == '.')
      schema = schema.substring(0, schema.length() - 1);
    sql.append(" AND owner = '").append(schema.toUpperCase()).append("'");

    BigDecimal count = (BigDecimal) SqlUtils.executeScalar(dataSource, sql.toString(),
        "wdk-check-table-exist");
    return (count.longValue() > 0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getDateDataType()
   */
  @Override
  public String getDateDataType() {
    return "TIMESTAMP";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getFloatDataType(int)
   */
  @Override
  public String getFloatDataType(int size) {
    return "FLOAT(" + size + ")";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#convertBoolean(boolean)
   */
  @Override
  public String convertBoolean(boolean value) {
    return value ? "1" : "0";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#dropTable(java.lang.String, java.lang.String)
   */
  @Override
  public void dropTable(DataSource dataSource, String schema, String table, boolean purge)
      throws SQLException {
    String name = "wdk-drop-table-" + table;
    String sql = "DROP TABLE ";
    if (schema != null)
      sql += schema;
    sql += table;
    if (purge) {
      sql += " PURGE";
      name += "_purge";
    }
    SqlUtils.executeUpdate(dataSource, sql, name);
  }

  /**
   * We clean and lock the stats on a table, so that Oracle will use Dynamic Sampling to compute execution
   * plans.
   * 
   * One use case of this is on WDK cache tables, which has frequent inserts and the stats (if turned on) can
   * get stale quickly, which may result in bad execution plans. Locking the stats will force Oracle to sample
   * the data in the cache table.
   * 
   * @see DBPlatform#disableStatistics(DataSource, String, String)
   */
  @Override
  public void disableStatistics(DataSource dataSource, String schema, String tableName) throws SQLException {
    schema = schema.trim().toUpperCase();
    if (schema.endsWith("."))
      schema = schema.substring(0, schema.length() - 1);
    tableName = tableName.toUpperCase();
    Connection connection = null;
    CallableStatement stUnlock = null, stDelete = null, stLock = null;
    try {
      connection = dataSource.getConnection();
      connection.setAutoCommit(false);

      stUnlock = connection.prepareCall(tableName);
      stUnlock.executeUpdate("{call DBMS_STATS.unlock_table_stats('" + schema + "', '" + tableName + "') }");
      stUnlock.executeUpdate();

      stDelete = connection.prepareCall(tableName);
      stDelete.executeUpdate("{call DBMS_STATS.DELETE_TABLE_STATS('" + schema + "', '" + tableName + "') }");
      stDelete.executeUpdate();

      stLock = connection.prepareCall(tableName);
      stLock.executeUpdate("{call DBMS_STATS.LOCK_TABLE_STATS('" + schema + "', '" + tableName + "') }");
      stLock.executeUpdate();

      connection.commit();
    }
    catch (SQLException e) {
      connection.rollback();
      throw e;
    }
    finally {
      SqlUtils.closeQuietly(stUnlock, stDelete, stLock);
      try {
        connection.setAutoCommit(true);
      }
      catch (SQLException e) {
        LOG.error("Unable to set connection's auto-commit back to true.", e);
      }
      SqlUtils.closeQuietly(connection);
    }
  }

  @Override
  public void computeThenLockStatistics(DataSource dataSource, String schema, String tableName) throws SQLException {
    schema = schema.trim().toUpperCase();
    if (schema.endsWith("."))
      schema = schema.substring(0, schema.length() - 1);
    tableName = tableName.toUpperCase();
    Connection connection = null;
    CallableStatement stUnlock = null, stCompute = null, stLock = null;
    try {

      connection = dataSource.getConnection();
      connection.setAutoCommit(false);

      stUnlock = connection.prepareCall(tableName);
      stUnlock.executeUpdate("{call DBMS_STATS.unlock_table_stats('" + schema + "', '" + tableName + "') }");
      stUnlock.executeUpdate();

      String sql = 
      "begin dbms_stats.gather_table_stats(ownname=> '" + schema + "', tabname=> '" + tableName +
      "', estimate_percent=> DBMS_STATS.AUTO_SAMPLE_SIZE, cascade=> TRUE, degree=> null, method_opt=> 'FOR ALL COLUMNS SIZE AUTO'); End;"; 

      stCompute = connection.prepareCall(tableName);
      stCompute.executeUpdate(sql);
      stCompute.executeUpdate();

      stLock = connection.prepareCall(tableName);
      stLock.executeUpdate("{call DBMS_STATS.LOCK_TABLE_STATS('" + schema + "', '" + tableName + "') }");
      stLock.executeUpdate();

      connection.commit();
    }
    catch (SQLException e) {
      connection.rollback();
      throw e;
    }
    finally {
      SqlUtils.closeQuietly(stUnlock, stCompute, stLock);
      try {
        connection.setAutoCommit(true);
      }
      catch (SQLException e) {
        LOG.error("Unable to set connection's auto-commit back to true.", e);
      }
      SqlUtils.closeQuietly(connection);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dbms.DBPlatform#getTables(java.lang.String, java.lang.String)
   */
  @Override
  public String[] queryTableNames(DataSource dataSource, String schema, String pattern) throws SQLException {
    String sql = "SELECT table_name FROM all_tables WHERE owner = '" + schema.toUpperCase() +
        "' AND table_name LIKE '" + pattern.toUpperCase() + "'";
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql, "wdk-oracle-select-table-names");
      List<String> tables = new ArrayList<String>();
      while (resultSet.next()) {
        tables.add(resultSet.getString("table_name"));
      }
      String[] array = new String[tables.size()];
      tables.toArray(array);
      return array;
    }
    finally {
      SqlUtils.closeResultSetAndStatement(resultSet, null);
    }
  }

  @Override
  public String getDummyTable() {
    return " FROM dual";
  }

  @Override
  public String getResizeColumnSql(String tableName, String column, int size) {
    return "ALTER TABLE " + tableName + " MODIFY (" + column + " varchar(" + size + ") )";
  }

  /**
   * The default schema is the same as current login.
   * 
   * @see org.gusdb.fgputil.db.platform.DBPlatform#getDefaultSchema(java.lang.String)
   */
  @Override
  public String getDefaultSchema(String login) {
    return normalizeSchema(login);
  }

  @Override
  public String getRowNumberColumn() {
    return "rownum";
  }

  @Override
  public int getBooleanType() {
    return Types.BIT;
  }
  
  private static final int EXPRESSION_LIMIT = 999;

  @Override
  public String prepareExpressionList(String[] values) {
    StringBuilder buffer = new StringBuilder();
    if (values.length <= EXPRESSION_LIMIT) { // Oracle has a hard limit on the # of items in one expression list
      appendItems(buffer, values, 0, values.length);
    }
    else { // over the limit, will need to convert the list into unions
      for (int i = 0; i < values.length; i += EXPRESSION_LIMIT) {
        if (buffer.length() > 0)
          buffer.append(" UNION ");
        buffer.append("SELECT * FROM table(SYS.DBMS_DEBUG_VC2COLL(");
        int end = Math.min(i + EXPRESSION_LIMIT, values.length);
        appendItems(buffer, values, i, end);
        buffer.append("))");
      }
    }
    return buffer.toString();
  }

  /**
   * append the values into the buffer, comma separated.
   * 
   * @param buffer
   * @param values
   * @param start
   *          the start of the values to be appended, inclusive.
   * @param end
   *          the end of the values to be appended. exclusive.
   */
  private void appendItems(StringBuilder buffer, String[] values, int start, int end) {
    for (int i = start; i < end; i++) {
      if (i > start)
        buffer.append(",");
      buffer.append(values[i]);
    }
  }

  private static final String UNCOMMITED_STATEMENT_CHECK_SQL =
      "SELECT COUNT(*)" +
      " FROM v$transaction t, v$session s, v$mystat m" +
      " WHERE t.ses_addr = s.saddr "+
      " AND s.sid = m.sid" +
      " AND ROWNUM = 1";

  @Override
  public boolean containsUncommittedActions(Connection c)
      throws SQLException, UnsupportedOperationException {
    final boolean[] result = { false };
    try {
      new SQLRunner(c, UNCOMMITED_STATEMENT_CHECK_SQL, "check-uncommitted-statements").executeQuery(new ResultSetHandler() {
        @Override public void handleResult(ResultSet rs) throws SQLException {
          if (!rs.next()) {
            throw new SQLException("Count query returned zero rows."); // should never happen
          }
          result[0] = (rs.getInt(1) > 0);
        }
      });
      return result[0];
    }
    catch (SQLRunnerException e) {
      if (e.getCause() instanceof SQLException) {
        throw (SQLException)e.getCause();
      }
      throw e;
    }
  }

  /**
   * A pre-close commit will help "clear" resources if close() is actually simply a return to a connection
   * pool.  Always return true since Oracle does not have a problem with commits at any point in a
   * connection's life cycle.
   * 
   * @param connectionAutoCommitValue connection's current value of isAutoCommit()
   */
  @Override
  public boolean shouldPerformPreCloseCommit(boolean connectionAutoCommitValue) {
    return true;
  }
}
