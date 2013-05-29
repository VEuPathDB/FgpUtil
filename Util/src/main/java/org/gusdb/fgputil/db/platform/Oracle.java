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
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.DBStateException;
import org.gusdb.fgputil.db.SqlUtils;

/**
 * @author Jerric Gao
 */
public class Oracle extends DBPlatform {

    private static final Logger LOG = Logger.getLogger(Oracle.class);
  
    private static final String DRIVER_NAME = "oracle.jdbc.OracleDriver";
    
    public Oracle() throws SQLException {
        super(DRIVER_NAME);
    }
    
    @Override
    public String getValidationQuery() {
      return "SELECT 'ok' FROM dual";
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#createSequence(java.lang.String,
     * int, int)
     */
    @Override
    public void createSequence(DataSource dataSource, String sequence, int start, int increment)
            throws SQLException {
        StringBuffer sql = new StringBuffer("CREATE SEQUENCE ");
        sql.append(sequence);
        sql.append(" START WITH ").append(start);
        sql.append(" INCREMENT BY ").append(increment);
        SqlUtils.executeUpdate(dataSource, sql.toString(),
                "wdk-create-sequence");
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
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getNextId(java.lang.String,
     * java.lang.String)
     */
    @Override
    public int getNextId(DataSource dataSource, String schema, String table) throws SQLException, DBStateException {
		schema = normalizeSchema(schema);

		StringBuffer sql = new StringBuffer("SELECT ");
		sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
		sql.append(".nextval FROM dual");
		BigDecimal id = (BigDecimal) SqlUtils.executeScalar(
            dataSource, sql.toString(), "wdk-select-next-id");
		return id.intValue();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.dbms.DBPlatform#getNextIdSqlExpression(java.lang.
     * String, java.lang.String)
     */
    @Override
    public String getNextIdSqlExpression(String schema, String table) {
        schema = normalizeSchema(schema);

        StringBuffer sql = new StringBuffer("");
        sql.append(schema).append(table).append(ID_SEQUENCE_SUFFIX);
        sql.append(".nextval");
        return sql.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getClobData(java.sql.ResultSet,
     * java.lang.String)
     */
    @Override
    public String getClobData(ResultSet rs, String columnName)
            throws SQLException {
        Clob messageClob = rs.getClob(columnName);
        if (messageClob == null) return null;
        return messageClob.getSubString(1, (int) messageClob.length());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getPagedSql(java.lang.String,
     * int, int)
     */
    @Override
    public String getPagedSql(String sql, int startIndex, int endIndex) {
        StringBuffer buffer = new StringBuffer();
        // construct the outer query
        buffer.append("SELECT lb.* FROM (");
        // construct the inner nested query
        buffer.append("SELECT ub.*, rownum AS row_index FROM (");
        buffer.append(sql);
        buffer.append(") ub WHERE rownum <= ").append(endIndex);
        buffer.append(") lb WHERE lb.row_index >= ").append(startIndex);
        return buffer.toString();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#isTableExist(java.lang.String)
     */
    @Override
    public boolean checkTableExists(DataSource dataSource, String schema, String tableName)
            throws SQLException, DBStateException {
        StringBuffer sql = new StringBuffer("SELECT count(*) FROM ALL_TABLES ");
        sql.append("WHERE table_name = '");
        sql.append(tableName.toUpperCase()).append("'");
        if (schema.charAt(schema.length() - 1) == '.')
            schema = schema.substring(0, schema.length() - 1);
        sql.append(" AND owner = '").append(schema.toUpperCase()).append("'");

        BigDecimal count = (BigDecimal) SqlUtils.executeScalar(
                dataSource, sql.toString(), "wdk-check-table-exist");
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
     * @see org.gusdb.wdk.model.dbms.DBPlatform#dropTable(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void dropTable(DataSource dataSource, String schema, String table, boolean purge)
            throws SQLException {
        String name = "wdk-drop-table-" + table;
        String sql = "DROP TABLE ";
        if (schema != null) sql = schema;
        sql += table;
        if (purge) {
            sql += " PURGE";
            name += "_purge";
        }
        SqlUtils.executeUpdate(dataSource, sql, name);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.gusdb.wdk.model.dbms.DBPlatform#disableStatistics(java.lang.String,
     * java.lang.String)
     */
    @Override
    public void disableStatistics(DataSource dataSource, String schema,
            String tableName) throws SQLException {
      schema = schema.toUpperCase();
      tableName = tableName.toUpperCase();
      Connection connection = null;
      CallableStatement stUnlock = null, stDelete = null, stLock = null;
      try {
        connection = dataSource.getConnection();
        connection.setAutoCommit(false);

        stUnlock = connection.prepareCall(tableName);
        stUnlock.executeUpdate("{call DBMS_STATS.unlock_table_stats('" + schema
            + "', '" + tableName + "') }");
        stUnlock.executeUpdate();

        stDelete = connection.prepareCall(tableName);
        stDelete.executeUpdate("{call DBMS_STATS.DELETE_TABLE_STATS('" + schema
            + "', '" + tableName + "') }");
        stDelete.executeUpdate();

        stLock = connection.prepareCall(tableName);
        stLock.executeUpdate("{call DBMS_STATS.LOCK_TABLE_STATS('" + schema
            + "', '" + tableName + "') }");
        stLock.executeUpdate();

        connection.commit();
      } catch (SQLException e) {
        connection.rollback();
        throw e;
      } finally {
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

    /*
     * (non-Javadoc)
     * 
     * @see org.gusdb.wdk.model.dbms.DBPlatform#getTables(java.lang.String,
     * java.lang.String)
     */
    @Override
    public String[] queryTableNames(DataSource dataSource, String schema, String pattern)
            throws SQLException {
        String sql = "SELECT table_name FROM all_tables WHERE owner = '"
                + schema.toUpperCase() + "' AND table_name LIKE '"
                + pattern.toUpperCase() + "'";
        ResultSet resultSet = null;
        try {
            resultSet = SqlUtils.executeQuery(dataSource, sql,
                    "wdk-oracle-select-table-names");
            List<String> tables = new ArrayList<String>();
            while (resultSet.next()) {
                tables.add(resultSet.getString("table_name"));
            }
            String[] array = new String[tables.size()];
            tables.toArray(array);
            return array;
        } finally {
            SqlUtils.closeResultSetAndStatement(resultSet);
        }
    }

    @Override
    public String getDummyTable() {
        return " FROM dual";
    }
    
    @Override
    public String getResizeColumnSql(String tableName, String column, int size) {
      return "ALTER TABLE " + tableName + " MODIFY (" + column
          + " varchar(" + size + ") )";
    }
}
