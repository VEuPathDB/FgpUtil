package org.gusdb.fgputil.db.platform;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;

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
        if (schema.length() > 0 && !schema.endsWith(".")) schema += ".";
        return schema;
    }

    public static String normalizeString(String string) {
        return string.replaceAll("'", "''");
    }

    //#########################################################################
    // platform-dependent abstract methods
    //#########################################################################

    public abstract int getNextId(DataSource dataSource, String schema,
        String table) throws SQLException, DBStateException;

    public abstract String getNextIdSqlExpression(String schema, String table);

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
     * inclusively; thus the range is [startIndex, endIndex]
     * 
     * @param sql SQL to wrap
     * @param startIndex 1-based start index (inclusive)
     * @param endIndex end index (inclusive)
     * @return wrapped SQL
     */
    public abstract String getPagedSql(String sql, int startIndex, int endIndex);

    public abstract boolean checkTableExists(DataSource dataSource, String schema, String tableName)
            throws SQLException, DBStateException;

    public abstract String convertBoolean(boolean value);

    public abstract void dropTable(DataSource dataSource, String schema, String table, boolean purge)
            throws SQLException;

    public abstract void disableStatistics(DataSource dataSource, String schema, String tableName) throws SQLException;

    public abstract String getDriverClassName();
    
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

    //#########################################################################
    // Common methods are platform independent
    //#########################################################################

    public int setClobData(PreparedStatement ps, int columnIndex,
        String content, boolean commit) throws SQLException {
      SqlUtils.setClobData(ps, columnIndex, content);
      return (commit ? ps.executeUpdate() : 0);
    }
}
