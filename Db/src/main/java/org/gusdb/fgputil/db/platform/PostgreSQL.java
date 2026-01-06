package org.gusdb.fgputil.db.platform;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.handler.SingleLongResultSetHandler;
import org.gusdb.fgputil.functional.Either;
import org.veupathdb.lib.ldap.NetDesc;
import org.veupathdb.lib.ldap.PostgresNetDesc;

/**
 * @author Jerric Gao
 */
public class PostgreSQL extends DBPlatform {

  public static final String DRIVER_NAME = "org.postgresql.Driver";

  public static final String CONNECTION_URL_SCHEME = "jdbc:postgresql://";

  // connection URL patterns
  private static final Pattern URL_PATTERN_WITH_PORT = Pattern.compile("^" + CONNECTION_URL_SCHEME + "([.a-zA-Z0-9_\\-]+):([0-9]+)/([.a-zA-Z0-9_\\-]+)$");
  private static final Pattern URL_PATTERN_WITHOUT_PORT = Pattern.compile("^" + CONNECTION_URL_SCHEME + "([.a-zA-Z0-9_\\-]+)/([.a-zA-Z0-9_\\-]+)$");

  public PostgreSQL() {
    super();
  }

  @Override
  public String getSysdateIdentifier(){
      return "LOCALTIMESTAMP";
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
  public String getConnectionUrl(String host, int port, String serviceName) {
    return CONNECTION_URL_SCHEME + host + ":" + port + "/" + serviceName;
  }

  @Override
  public Either<NetDesc,String> parsePlatformConnectionUrl(String connectionUrl) {
    // try with and without port, with first
    Matcher m = URL_PATTERN_WITH_PORT.matcher(connectionUrl);
    if (m.find()) {
      return Either.left(new PostgresNetDesc(m.group(1), Integer.parseInt(m.group(2)), m.group(3)));
    }
    m = URL_PATTERN_WITHOUT_PORT.matcher(connectionUrl);
    if (m.find()) {
      return Either.left(new PostgresNetDesc(m.group(1), getDefaultPort(), m.group(2)));
    }
    throw new IllegalArgumentException("Unsupported Postgres connection URL: " + connectionUrl);
  }

  @Override
  public String getValidationQuery() {
    return "SELECT 'ok'";
  }

  @Override
  public void createSequence(DataSource dataSource, String sequence, int start, int increment)
      throws SQLException {
    String sql = new StringBuilder("CREATE SEQUENCE ")
        .append(sequence)
        .append(" START ")
        .append(start)
        .append(" INCREMENT ")
        .append(increment)
        .toString();
    new SQLRunner(dataSource, sql, "wdk-create-sequence").executeUpdate();
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
    String sql = new StringBuilder("SELECT nextval('")
        .append(schema).append(table).append(ID_SEQUENCE_SUFFIX).append("')").toString();
    return new SQLRunner(dataSource, sql, "select-next-id").executeQuery(new SingleLongResultSetHandler()).get();
  }

  @Override
  public String getNextIdSqlExpression(String schema, String table) {
    return getNextValExpression(schema, table, ID_SEQUENCE_SUFFIX);
  }

  @Override
  public String getNextValExpression(String schema, String table, String sequenceSuffix){
    schema = normalizeSchema(schema);

    StringBuffer sql = new StringBuffer("nextval('");
    sql.append(schema).append(table).append(sequenceSuffix);
    sql.append("')");
    return sql.toString();
  }

  @Override
  public String getNumberDataType(int size) {
    return "NUMERIC(" + size + ")";
  }

  /**
   * NOTE: For performance reasons, the LIMIT and OFFSET have been moved directly next to the
   * passed SQL (not outside parens).  Doing so has shown a significant performance improvement.
   * This means if a limit/offset is already at the end of the passed SQL, this method will
   * return malformed SQL.  To avoid this, you can wrap the passed SQL in parens before passing.
   */
  @Override
  public String getPagedSql(String sql, int startIndex, int endIndex, boolean includeRowIndex) {
    String rowIndex = includeRowIndex ? ", " + getRowNumberColumn() + " as row_index " : "";
    StringBuilder buffer = new StringBuilder("SELECT f.*" + rowIndex + " FROM ");
    buffer.append("(").append(sql);
    if (endIndex > -1) {
      buffer.append(" LIMIT ").append(endIndex - startIndex + 1);
    }
    buffer.append(" OFFSET ").append(startIndex - 1).append(") f ");
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

    String sql = new StringBuilder("SELECT count(*) FROM pg_tables ")
        .append("WHERE tablename = '").append(tableName).append("'")
        .append(" AND schemaname = '").append(schema).append("'")
        .toString();
    long count = new SQLRunner(dataSource, sql, "wdk-check-table-exist").executeQuery(new SingleLongResultSetHandler()).get();
    return count > 0;
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
    new SQLRunner(dataSource, sql, "wdk-drop-table" + table).executeStatement();
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
    String sql =
        "SELECT tablename" +
        " FROM pg_tables" +
        " WHERE lower(schemaname) = lower('" + denormalizeSchema(schema) + "')" +
        " AND lower(tablename) LIKE lower('" + pattern + "')";
    return new SQLRunner(dataSource, sql, "wdk-postgres-select-table-names").executeQuery(resultSet -> {
      List<String> tables = new ArrayList<String>();
      while (resultSet.next()) {
        tables.add(resultSet.getString("tablename"));
      }
      String[] array = new String[tables.size()];
      tables.toArray(array);
      return array;
    });
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

  @Override
  public SupportedPlatform getPlatformEnum() {
    return SupportedPlatform.POSTGRESQL;
  }
}
