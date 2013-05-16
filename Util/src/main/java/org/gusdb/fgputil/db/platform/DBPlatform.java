package org.gusdb.fgputil.db.platform;

import java.io.StringReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.DBStateException;

/**
 * Provides a base class for DB-vendor-specific interfaces.  This allows calling
 * code to make requests of the database without knowing the underlying vendor.
 * 
 * @author Jerric Gao
 * @modified Ryan Doherty
 */
public abstract class DBPlatform {

    public static final String ID_SEQUENCE_SUFFIX = "_pkseq";

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(DBPlatform.class);

    //#########################################################################
    // Platform-related static helper functions
    //#########################################################################

    public DBPlatform(String driverClassName) throws SQLException {
      registerDriver(driverClassName);
    }

    private static void registerDriver(String driverClassName) throws SQLException {
      try {
        // register the driver
        @SuppressWarnings("unchecked")
        Class<? extends Driver> driverClass = (Class<? extends Driver>)Class.forName(driverClassName);
        DriverManager.registerDriver(driverClass.newInstance());
        //System.setProperty("jdbc.drivers", DRIVER_NAME);
      }
      catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
        throw new SQLException("Unable to register Oracle driver.  " +
            "Is the appropriate ojdbc jar file in your classpath?", e);
      }
    }
    
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

    public abstract String getDateDataType();

    public abstract String getMinusOperator();

    public abstract void createSequence(DataSource dataSource, String sequence, int start,
            int increment) throws SQLException;
    
    public abstract String getClobData(ResultSet rs, String columnName)
            throws SQLException;

    public abstract String getPagedSql(String sql, int startIndex, int endIndex);

    public abstract boolean checkTableExists(DataSource dataSource, String schema, String tableName)
            throws SQLException, DBStateException;

    public abstract String convertBoolean(boolean value);

    public abstract void dropTable(DataSource dataSource, String schema, String table, boolean purge)
            throws SQLException;

    public abstract void disableStatistics(Connection connection,
            String schema, String tableName) throws SQLException;

    public abstract String getValidationQuery();
    
    /**
     * @param dataSource data source to use
     * @param schema
     *            the schema cannot be empty. if you are searching in a local
     *            schema, it has to be the login user name.
     * @param pattern
     * @return list of table names
     * @throws SQLException
     */
    public abstract String[] queryTableNames(DataSource dataSource, String schema, String pattern)
            throws SQLException;
    
    public abstract String getDummyTable();

    public abstract String getResizeColumnSql(String tableName, String column, int size);

    //#########################################################################
    // Common methods are platform independent
    //#########################################################################

    public int setClobData(PreparedStatement ps, int columnIndex,
            String content, boolean commit) throws SQLException {
      StringReader reader = new StringReader(content);
      ps.setCharacterStream(columnIndex, reader, content.length());
      return (commit ? ps.executeUpdate() : 0);
    }
}
