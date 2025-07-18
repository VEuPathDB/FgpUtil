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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.SQLRunner;
import org.gusdb.fgputil.db.runner.SQLRunnerException;
import org.gusdb.fgputil.functional.Either;
import org.veupathdb.lib.ldap.NetDesc;
import org.veupathdb.lib.ldap.OracleNetDesc;

/**
 * @author Jerric Gao
 */
public class Oracle extends DBPlatform {

  private static final Logger LOG = Logger.getLogger(Oracle.class);

  public static final String DRIVER_NAME = "oracle.jdbc.driver.OracleDriver";

  // connection URL prefixes
  public static final String CONNECTION_URL_PREFIX = "jdbc:oracle:";
  public static final String CONNECTION_URL_SCHEME_THIN = CONNECTION_URL_PREFIX + "thin:@//";
  public static final String CONNECTION_URL_SCHEME_OCI = CONNECTION_URL_PREFIX + "oci:@";
  public static final String CONNECTION_URL_SCHEME_TNS = CONNECTION_URL_PREFIX + "thin:@";

  // connection URL patterns
  private static final Pattern URL_PATTERN_THIN_WITH_PORT = Pattern.compile("^" + CONNECTION_URL_SCHEME_THIN + "([\\.a-zA-Z0-9_\\-]+):([0-9]+)/([\\(\\)\\.a-zA-Z0-9_\\-]+)$");
  private static final Pattern URL_PATTERN_THIN_WITHOUT_PORT = Pattern.compile("^" + CONNECTION_URL_SCHEME_THIN + "([\\.a-zA-Z0-9_\\-]+)/([\\(\\)\\.a-zA-Z0-9_\\-]+)$");
  private static final Pattern URL_PATTERN_OCI = Pattern.compile("^" + CONNECTION_URL_SCHEME_OCI + "([\\.a-zA-Z0-9_\\-]+)$");
  private static final Pattern URL_PATTERN_TNS = Pattern.compile("^" + CONNECTION_URL_SCHEME_TNS + "(.+)");

  public Oracle() {
    super();
  }

  @Override
  public String getSysdateIdentifier(){
    return "SYSDATE";
  }

  @Override
  public String getNvlFunctionName() {
	  return "NVL";
  }

  @Override
  public String getDriverClassName() {
    return DRIVER_NAME;
  }

  @Override
  public String getConnectionUrl(String host, int port, String serviceName) {
    return CONNECTION_URL_SCHEME_THIN + host + ":" + port + "/" + serviceName;
  }

  @Override
  public Either<NetDesc,String> parsePlatformConnectionUrl(String connectionUrl) {
    // try with and without port, with first
    Matcher m = URL_PATTERN_THIN_WITH_PORT.matcher(connectionUrl);
    if (m.find()) {
      return Either.left(new OracleNetDesc(m.group(1), Integer.parseInt(m.group(2)), m.group(3)));
    }
    m = URL_PATTERN_THIN_WITHOUT_PORT.matcher(connectionUrl);
    if (m.find()) {
      return Either.left(new OracleNetDesc(m.group(1), getDefaultPort(), m.group(2)));
    }
    m = URL_PATTERN_OCI.matcher(connectionUrl);
    if (m.find()) {
      return Either.right(m.group(1));
    }
    m = URL_PATTERN_TNS.matcher(connectionUrl);
    if (m.find()) {
      String tnsFormat = m.group(1);
      try {
        return Either.left(new OracleNetDesc(tnsFormat));
      }
      catch (IllegalArgumentException e) {
        // fall through to the throw below
      }
    }
    throw new IllegalArgumentException("Unsupported Oracle connection URL: " + connectionUrl);
  }

  @Override
  public String getValidationQuery() {
    return "SELECT 'ok' FROM dual";
  }

  @Override
  public void createSequence(DataSource dataSource, String sequence, int start, int increment)
      throws SQLException {
    StringBuilder sql = new StringBuilder("CREATE SEQUENCE ");
    sql.append(sequence);
    sql.append(" START WITH ").append(start);
    sql.append(" INCREMENT BY ").append(increment);
    SqlUtils.executeUpdate(dataSource, sql.toString(), "wdk-create-sequence");
  }

  @Override
  public String getBooleanDataType() {
    return "NUMBER(1)";
  }

  @Override
  public String getNumberDataType(int size) {
    return "NUMBER(" + size + ")";
  }

  @Override
  public String getStringDataType(int size) {
    return "VARCHAR(" + size + ")";
  }

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

  @Override
  public String getMinusOperator() {
    return "MINUS";
  }

  @Override
  public long getNextId(DataSource dataSource, String schema, String table) throws SQLException {
    return getNextNIds(dataSource, schema, table, 1).get(0);
  }

  @Override
  public List<Long> getNextNIds(DataSource dataSource, String schema, String table, int numIds) throws SQLException {
    if (numIds <= 0) throw new IllegalArgumentException("Must request >0 IDs. " + numIds + " passed.");
    String sql = new StringBuilder()
      .append("SELECT ")
      .append(getNextIdSqlExpression(schema, table))
      .append(" FROM dual connect by level <= " + numIds)
      .toString();
    return new SQLRunner(dataSource, sql, "select-next-n-ids").executeQuery(rs -> {
      List<Long> ids = new ArrayList<>();
      while (rs.next()) {
        ids.add(rs.getLong(1));
      }
      return ids;
    });
  }

  @Override
  public String getNextIdSqlExpression(String schema, String table) {
    return getNextValExpression(schema, table, ID_SEQUENCE_SUFFIX);
  }

  @Override
  public String getNextValExpression(String schema, String table, String sequenceSuffix){
    return normalizeSchema(schema) + table + sequenceSuffix + ".nextval";
  }

  @Override
  public String getClobData(ResultSet rs, String columnName) throws SQLException {
    Clob messageClob = rs.getClob(columnName);
    if (messageClob == null)
      return null;
    String data = messageClob.getSubString(1, (int) messageClob.length());
    messageClob.free();
    return data;
  }

  @Override
  public String getPagedSql(String sql, int startIndex, int endIndex, boolean includeRowIndex) {

    String rowIndex = includeRowIndex? ", " + getRowNumberColumn() + " as row_index " : "";
    int offset = (startIndex <= 0 ? 0 : startIndex - 1);
    int fetchSize =
        endIndex < 0 ? -1 :      // all rows
        endIndex <= offset ? 0 : // no rows
        endIndex - offset;       // some rows
    String fetchClause = (fetchSize < 0 ? "" :
      " FETCH NEXT " + fetchSize + " ROWS ONLY");

    return new StringBuilder()
        .append("SELECT a.*")
        .append(rowIndex)
        .append(" FROM ( ")
        .append(sql)
        .append(" ) a")
        .append(" OFFSET ").append(offset).append(" ROWS")
        .append(fetchClause)
        .toString();
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

  @Override
  public String getDateDataType() {
    return "TIMESTAMP";
  }

  @Override
  public String getFloatDataType(int size) {
    return "FLOAT(" + size + ")";
  }

  @Override
  public Integer convertBoolean(boolean value) {
    return value ? 1 : 0;
  }

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
    } else { // over the limit, will need to convert the list into unions
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
    try {
      return new SQLRunner(c, UNCOMMITED_STATEMENT_CHECK_SQL, "check-uncommitted-statements")
        .executeQuery(rs -> {
          if (!rs.next()) {
            throw new SQLException("Count query returned zero rows."); // should never happen
          }
          return (rs.getInt(1) > 0);
        });
    }
    catch (SQLRunnerException e) {
      if (e.getCause() instanceof SQLException) {
        throw (SQLException)e.getCause();
      }
      throw e;
    }
  }

  @Override
  public SupportedPlatform getPlatformEnum() {
    return SupportedPlatform.ORACLE;
  }
}
