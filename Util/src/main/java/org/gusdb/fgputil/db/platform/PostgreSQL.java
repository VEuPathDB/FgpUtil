package org.gusdb.fgputil.db.platform;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;

/**
 * @author Jerric Gao
 */
public class PostgreSQL extends DBPlatform {

  public static final String DRIVER_NAME = "org.postgresql.Driver";

  public PostgreSQL() {
    super();
  }

  @Override
  public String getNvlFunctionName() {
	  return "COALESCE";
  }

  @Override
  public String getDriverClassName() {
    return DRIVER_NAME;
  }

  @Override
  public String getValidationQuery() {
    return "SELECT 'ok'";
  }

  @Override
  public void createSequence(DataSource dataSource, String sequence, int start, int increment)
      throws SQLException {
    StringBuffer sql = new StringBuffer("CREATE SEQUENCE ");
    sql.append(sequence);
    sql.append(" START ");
    sql.append(start);
    sql.append(" INCREMENT ");
    sql.append(increment);
    SqlUtils.executeUpdate(dataSource, sql.toString(), "wdk-create-sequence");
  }

  @Override
  public String getBooleanDataType() {
    return "boolean";
  }

  @Override
  public String getClobData(ResultSet rs, String columnName) throws SQLException {
    return rs.getString(columnName);
  }

  @Override
  public String getClobDataType() {
    return "text";
  }

  @Override
  public String getBlobDataType() {
    return "BYTEA";
  }

  @Override
  public int getBlobSqlType() {
    return Types.LONGVARBINARY;
  }

  @Override
  public String getMinusOperator() {
    return "EXCEPT";
  }

  @Override
  public long getNextId(DataSource dataSource, String schema, String table) throws SQLException {
    schema = normalizeSchema(schema);
    StringBuffer sql = new StringBuffer("SELECT nextval('");
    sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
    sql.append("')");
    long id = (Long) SqlUtils.executeScalar(dataSource, sql.toString(), "select-next-id");
    return id;
  }

  @Override
  public String getNextIdSqlExpression(String schema, String table) {
    schema = normalizeSchema(schema);

    StringBuffer sql = new StringBuffer("nextval('");
    sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
    sql.append("')");
    return sql.toString();
  }

  @Override
  public String getNumberDataType(int size) {
    return "NUMERIC(" + size + ")";
  }

  @Override
  public String getPagedSql(String sql, int startIndex, int endIndex, boolean includeRowIndex) {
    String rowIndex = includeRowIndex? ", " + getRowNumberColumn() + " as row_index " : "";
    StringBuffer buffer = new StringBuffer("SELECT f.*" + rowIndex + " FROM ");
    buffer.append("(").append(sql).append(") f ");
    if (endIndex > -1) {
      buffer.append(" LIMIT ").append(endIndex - startIndex + 1);
    }
    buffer.append(" OFFSET ").append(startIndex - 1);
    return buffer.toString();
  }

  @Override
  public String getStringDataType(int size) {
    return "VARCHAR(" + size + ")";
  }

  /**
   * Check the existence of a table. If the schema is null or empty, the schema will will be ignored, and will
   * look up the table in the public schema.
   *
   * @see org.gusdb.fgputil.db.platform.DBPlatform#checkTableExists(DataSource, String, String)
   */
  @Override
  public boolean checkTableExists(DataSource dataSource, String schema, String tableName)
      throws SQLException, DBStateException {
    if (schema.endsWith("."))
      schema = schema.substring(0, schema.length() - 1);
    tableName = tableName.toLowerCase();

    StringBuffer sql = new StringBuffer("SELECT count(*) FROM pg_tables ");
    sql.append("WHERE tablename = '").append(tableName).append("'");
    sql.append(" AND schemaname = '").append(schema).append("'");
    long count = (Long) SqlUtils.executeScalar(dataSource, sql.toString(), "wdk-check-table-exist");
    return (count > 0);
  }

  @Override
  public String getDateDataType() {
    return "TIMESTAMP";
  }

  @Override
  public String getFloatDataType(int size) {
    return "FLOAT(" + size + ")";
  }

  @Override
  public Boolean convertBoolean(boolean value) {
    return value;
  }

  @Override
  public void dropTable(DataSource dataSource, String schema, String table, boolean purge)
      throws SQLException {
    String sql = "DROP TABLE ";
    if (schema != null)
      sql += schema;
    sql += table;
    // ignore purge option
    SqlUtils.executeUpdate(dataSource, sql, "wdk-drop-table" + table);
  }

  @Override
  public void disableStatistics(DataSource dataSource, String schema, String tableName) {
    // do nothing in PSQL.
  }

  @Override
  public void computeThenLockStatistics(DataSource dataSource, String schema, String tableName) {
    // do nothing in PSQL.
  }


  @Override
  public String[] queryTableNames(DataSource dataSource, String schema, String pattern) throws SQLException {
    String sql = "SELECT tablename FROM pg_tables WHERE schemaname = '" + schema + "' AND tablename LIKE '" +
        pattern + "'";
    ResultSet resultSet = null;
    try {
      resultSet = SqlUtils.executeQuery(dataSource, sql, "wdk-postgres-select-table-names");
      List<String> tables = new ArrayList<String>();
      while (resultSet.next()) {
        tables.add(resultSet.getString("tablename"));
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
    return " ";
  }

  @Override
  public String getResizeColumnSql(String tableName, String column, int size) {
    return "ALTER TABLE " + tableName + " ALTER COLUMN " + column + " TYPE varchar(" + size + ")";
  }

  /**
   * the default schema in PostgreSQL is not the current login, it's public
   */
  @Override
  public String getDefaultSchema(String login) {
    return normalizeSchema("public");
  }

  @Override
  public String getRowNumberColumn() {
    return "row_number() over()";
  }

  @Override
  public int getBooleanType() {
    return Types.BOOLEAN;
  }

  @Override
  public String prepareExpressionList(String[] values) {
    return FormatUtil.join(values, ",");
  }

  /**
   * Postgres implementation does not yet support this method
   * TODO: Support this method; information on a possible solution might be found here:
   * http://stackoverflow.com/questions/1651219/how-to-check-for-pending-operations-in-a-postgresql-transaction
   */
  @Override
  public boolean containsUncommittedActions(Connection c)
      throws SQLException, UnsupportedOperationException {
    throw new UnsupportedOperationException("Method not yet supported.");
  }
}
