package org.gusdb.fgputil.db.platform;

import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.functional.Either;
import org.veupathdb.lib.ldap.NetDesc;

/**
 * Provides a base class for DB-vendor-specific interfaces.  This allows calling
 * code to make requests of the database without knowing the underlying vendor.
 *
 * @author Jerric Gao
 * @author Ryan Doherty
 */
public abstract class DBPlatform {

    public static final String ID_SEQUENCE_SUFFIX = "_pkseq";

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DBPlatform.class);

    //#########################################################################
    // Platform-related static helper functions
    //#########################################################################

    /**
     * Normalize the schema name.  If not empty, a dot will be appended to the
     * end of it.
     *
     * @param schema schema name
     * @return normalized schema
     */
    public static String normalizeSchema(String schema) {
        if (schema == null) return "";
        schema = schema.trim().toLowerCase();
        if (!schema.isEmpty() && !schema.endsWith(".")) schema += ".";
        return schema;
    }

    public static String normalizeString(String string) {
        return string.replaceAll("'", "''");
    }

    public static Either<NetDesc, String> parseConnectionUrl(String connectionUrl) {
      return SupportedPlatform
          .fromConnectionUrl(connectionUrl)
          .getPlatformInstance()
          .parsePlatformConnectionUrl(connectionUrl);
    }

    public static String getConnectionUrl(NetDesc netDesc) {
      return SupportedPlatform
          .fromLdapPlatform(netDesc.getPlatform())
          .getPlatformInstance()
          .getConnectionUrl(netDesc.getHost(), netDesc.getPort(), netDesc.getIdentifier());
    }

    //#########################################################################
    // platform-dependent abstract methods
    //#########################################################################

    public abstract SupportedPlatform getPlatformEnum();

    public abstract long getNextId(DataSource dataSource, String schema, String table) throws SQLException;

    public abstract String getNextIdSqlExpression(String schema, String table);

    public abstract String getNextValExpression(String schema, String table, String sequenceSuffix);

    public abstract String getNumberDataType(int size);

    public abstract String getFloatDataType(int size);

    public abstract String getStringDataType(int size);

    public abstract String getBooleanDataType();

    public abstract String getClobDataType();

    public abstract String getBlobDataType();

    public abstract int getBlobSqlType();

    public abstract String getDateDataType();

    public abstract String getMinusOperator();

    public abstract int getBooleanType();

    public abstract void createSequence(DataSource dataSource, String sequence, int start,
            int increment) throws SQLException;

    public abstract String getClobData(ResultSet rs, String columnName)
            throws SQLException;

    /**
     * Returns the passed SQL wrapped in a superquery that returns only the
     * subset of records defined by startIndex and endIndex.  Indexing is
     * 1-based (i.e. first index is 1) and the query will select the records
     * inclusively; thus the range is [startIndex, endIndex].  If all remaining
     * records are desired, pass a negative value for endIndex.
     *
     * @param sql SQL to wrap
     * @param startIndex 1-based start index (inclusive)
     * @param endIndex end index (inclusive), or a negative value for all records
     * @return wrapped SQL
     */
    public abstract String getPagedSql(String sql, int startIndex, int endIndex, boolean includeRowIndex);

    /**
     * Returns whether the given table exists
     *
     * @param dataSource data source to query
     * @param schema schema to check
     * @param tableName name of table to check
     * @return true if table exists; else false
     * @throws SQLException if unable to confirm existence of table
     */
    public abstract boolean checkTableExists(DataSource dataSource, String schema, String tableName)
            throws SQLException;

    public abstract Object convertBoolean(boolean value);

    public abstract void dropTable(DataSource dataSource, String schema, String table, boolean purge)
            throws SQLException;

    public abstract void disableStatistics(DataSource dataSource, String schema, String tableName) throws SQLException;

    public abstract void computeThenLockStatistics(DataSource dataSource, String schema, String tableName) throws SQLException;

    public abstract String getDriverClassName();

    public abstract String getConnectionUrl(String host, int port, String identifier);

    public abstract Either<NetDesc,String> parsePlatformConnectionUrl(String connectionUrl);

    public abstract String getValidationQuery();

    /**
     * This method consults the database and checks whether any insert, update, or delete
     * statements have been executed on this transaction but not yet committed.
     *
     * @param c connection to check
     * @return true if uncommitted operations have been performed; else false
     * @throws SQLException if error occurs while attempting determination (also if permission denied)
     * @throws UnsupportedOperationException if this method is unsupported in the platform implementation
     */
    public abstract boolean containsUncommittedActions(Connection c)
        throws SQLException, UnsupportedOperationException;

    /**
     *
     *
     * @param dataSource data source to use
     * @param schema schema name. The schema cannot be empty. If you are searching
     *        in a local schema, the login user name should be used.
     * @param pattern pattern to match table names against; this may be platform-specific
     * @return list of table names
     * @throws SQLException if error occurs
     */
    public abstract String[] queryTableNames(DataSource dataSource, String schema, String pattern)
            throws SQLException;

    public abstract String getDummyTable();

    public abstract String getResizeColumnSql(String tableName, String column, int size);

    public abstract String getDefaultSchema(String login);

    public abstract String getRowNumberColumn();

    public abstract String prepareExpressionList(String[] values);

    public abstract String getNvlFunctionName();

    public abstract String getSysdateIdentifier();

    //#########################################################################
    // Common methods are platform independent
    //#########################################################################

    public int getDefaultPort() {
      return getPlatformEnum().getDefaultPort();
    }

    public int setClobData(PreparedStatement ps, int columnIndex,
        String content, boolean commit) throws SQLException {
      SqlUtils.setClobData(ps, columnIndex, content);
      return (commit ? ps.executeUpdate() : 0);
    }

    /**
     * Always use setCharacterStream method when streaming data from a reader
     * to a clob; postgres does NOT support setClob and will fail at runtime.
     * 
     * @param ps
     * @param columnIndex
     * @param content
     * @throws SQLException
     */
    public void setClob(PreparedStatement ps, int columnIndex, Reader content) throws SQLException {
      ps.setCharacterStream(columnIndex, content);
    }

    public Boolean getBooleanValue(ResultSet rs, String columnName, Boolean nullValue) throws SQLException {
      Boolean value = rs.getBoolean(columnName);
      return (rs.wasNull() ? nullValue : value);
    }

    /**
     * Return a list of unique IDs fetched from the DB's next value mechanism
     * TODO: make Postgres version as efficient as Oracle version and make this method abstract
     * 
     * @param dataSource data source providing IDs
     * @param schema schema containing sequence
     * @param table table name (sequence name will match
     * @param numIds number of IDs to fetch
     * @return
     * @throws SQLException
     */
    public List<Long> getNextNIds(DataSource dataSource, String schema, String table, int numIds) throws SQLException {
      List<Long> ids = new ArrayList<>();
      for (int i = 0; i < numIds; i++) {
        ids.add(getNextId(dataSource, schema, table));
      }
      return ids;
    }

    /**
     * Returns a SQL value containing a call to the DB's to_date() function
     * using the following format: 'YYYY-MM-DD"T"HH24:MI:SS'.  Note milliseconds
     * and timezone/offset are not supported.
     *
     * @param iso8601FormattedDateTime a date-time in ISO-8601 format (not including ms or tz/offset info)
     * @return date value to be used in select and conditional SQL
     */
    public String toDbDateSqlValue(String iso8601FormattedDateTime) {
      return "TO_DATE('" + iso8601FormattedDateTime + "','YYYY-MM-DD\"T\"HH24:MI:SS')";
    }

    /**
     * Returns a SQL value containing a call to the DB's to_date() function. The
     * passed Date is formatted into a ISO-8601 format and passed to
     * toDbDateSqlValue(String).
     *
     * @param date date value to be used in SQL
     * @return date value to be used in select and conditional SQL
     */
    public String toDbDateSqlValue(Date date) {
      return toDbDateSqlValue(FormatUtil.formatDateTimeNoTimezone(date));
    }

    /**
     * Returns a SQL value containing a call to the DB's to_date() function. The
     * passed dateTime is converted first to a java.util.Date, which is then
     * formatted into ISO-8601 format and passed to toDbDateSqlValue(String).
     * Note this method may have timezone issues given the call to FormatUtil's
     * date conversion library (TODO to be figured out).
     *
     * @param dateTime date-time value to be used in SQL
     * @return date value to be used in select and conditional SQL
     */
    public String toDbDateSqlValue(LocalDateTime dateTime) {
      return toDbDateSqlValue(FormatUtil.toDate(dateTime));
    }
}
