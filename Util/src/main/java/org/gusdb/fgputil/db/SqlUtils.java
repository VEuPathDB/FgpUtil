package org.gusdb.fgputil.db;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.sql.Wrapper;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.pool.DatabaseInstance;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.functional.FunctionalInterfaces.ConsumerWithException;
import org.gusdb.fgputil.iterator.Cursor;

/**
 * @author Jerric Gao
 */
public final class SqlUtils {
  
  public static final int DEFAULT_FETCH_SIZE = 100;

  private static final Logger logger = Logger.getLogger(SqlUtils.class);

  /**
   * private constructor, make sure SqlUtils cannot be instanced.
   */
  private SqlUtils() {}

  /**
   * Close the resultSet and the underlying statement, connection. Log the query.
   * 
   * @param resultSet
   *          result set to close
   */
  public static void closeResultSetAndStatement(ResultSet resultSet, Statement stmt) {
    try {
      if (resultSet != null) {
        // close the statement in any way
        Statement stmtLocal = null;
        try {
          try {
            stmtLocal = resultSet.getStatement();
          }
          finally {
            closeResultSetOnly(resultSet);
          }
        }
        finally {
          closeStatement(stmtLocal);
        }
      }
      if (resultSet == null || (stmt != null && !stmt.isClosed())) {
        closeStatement(stmt);
      }
    }
    catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Close the resultSet but not its statement. Log the query.
   * 
   * @param resultSet
   *          result set to close
   */
  public static void closeResultSetOnly(ResultSet resultSet) {
    try {

      // close our resultSet,remove from hash and write to log
      if (resultSet != null) {
        resultSet.close();
        QueryLogger.logEndResultsProcessing(resultSet);
      }

      // log orphaned result sets, ie, those that are closed but still in hash
      QueryLogger.logOrphanedResultSets();

    }
    catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Close the statement and underlying connection
   * 
   * @param stmt
   *          statement to close
   */
  public static void closeStatement(Statement stmt) {
    if (stmt != null) {
      Connection connection = null;
      try {
        if (stmt.isClosed()) return; // required because getConnection throws exception if already closed
        try {
          connection = ConnectionMapping.getConnection(stmt);
        }
        finally {
          // close statement regardless of whether we
          // succeed in getting connection
          closeQuietly(stmt);
        }
      }
      catch (SQLException e) {
        // don't make calling code handle inability to fetch underlying connection
        throw new RuntimeException(e);
      }
      finally {
        // unclear what happened above, but connection must be closed
        closeQuietly(connection);
      }
    }
  }

  public static PreparedStatement getPreparedStatement(DataSource dataSource, String sql) throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;

    try {
      connection = ConnectionMapping.getConnection(dataSource);
      ps = connection.prepareStatement(sql);
      ps.setFetchSize(100);
      return ps;
    }
    catch (SQLException ex) {
      closeStatement(ps);

      if (ps == null && connection != null) 
        SqlUtils.closeQuietly(connection);
  
      throw new SQLException("Failed to prepare query: \n" + sql + getUrlAndUser(connection), ex);
    }
  }

  /**
   * execute the update, and returns the number of rows affected.
   * 
   * @param stmt
   *          statement to execute
   * @param sql
   *          SQL inside prepared statement (for logging purposes)
   * @param name
   *          name of operation (for logging purposes)
   * @return true if statement succeeded; else false
   * @throws SQLException
   *           if unable to execute statement
   */
  public static boolean executePreparedStatement(PreparedStatement stmt, String sql, String name)
      throws SQLException {
    
    Connection connection = stmt.getConnection();

    try {
      long start = System.currentTimeMillis();
      boolean result = stmt.execute();
      QueryLogger.logEndStatementExecution(sql, name, start);
      return result;
    }
    catch (SQLException ex) {
      throw new SQLException("Failed to execute statement: \n" + sql + getUrlAndUser(connection), ex);
    }
  }
  
  /**
   * Executes a batch update; returns numbers of rows affected by each batch
   * 
   * @param stmt
   *          PrepatedStatement to which batches were added
   * @param sql
   *          SQL of prepared statement (for logging)
   * @param name
   *          name of this operation (for logging)
   * @return an array of row counts; each item is the # of rows updated by a batch of params added to the
   *         statement
   * @throws SQLException
   *           if unable to execute batch
   */
  public static int[] executePreparedStatementBatch(PreparedStatement stmt, String sql, String name)
      throws SQLException {
    
    Connection connection = stmt.getConnection();

    try {
      long start = System.currentTimeMillis();
      int[] numUpdates = stmt.executeBatch();
      QueryLogger.logEndStatementExecution(sql, name, start);
      return numUpdates;
    }
    catch (SQLException ex) {
      throw new SQLException("Failed to execute statement batch: \n" + sql + getUrlAndUser(connection), ex);
    }
  }

  /**
   * execute the update, and returns the number of rows affected.
   * 
   * @param dataSource
   *          data source from which to get connection on which to execute update
   * @param sql
   *          SQL to execute
   * @param name
   *          name of operation (for logging purposes)
   * @return number of rows affected
   * @throws SQLException
   *           if problem executing update
   */
  public static int executeUpdate(DataSource dataSource, String sql, String name) throws SQLException {
    return executeUpdate(dataSource, sql, name, false);
  }

  /**
   * execute the update, and returns the number of rows affected.
   * 
   * @param dataSource
   *          data source from which to get connection on which to execute update
   * @param sql
   *          SQL to execute
   * @param name
   *          name of operation (for logging purposes)
   * @param useDBLink
   *          a flag indicating if a dblink is used in this sql; If a dblink is used, a commit will be called
   *          before execution, to make sure the query get the latest copy of data from dblink. This is an
   *          Oracle only requirement.
   * @return number of rows affected
   * @throws SQLException
   *           if problem executing update
   */
  public static int executeUpdate(DataSource dataSource, String sql, String name, boolean useDBLink)
      throws SQLException {
    logger.trace("running sql: " + name + "\n" + sql);
    Connection connection = null;
    Statement stmt = null;
    
    try {
      long start = System.currentTimeMillis();
      connection = dataSource.getConnection();
      stmt = connection.createStatement();
      int result = stmt.executeUpdate(sql);
      QueryLogger.logEndStatementExecution(sql, name, start);
      return result;
    }
    catch (SQLException ex) {
      throw new SQLException("Failed to run SQL:\n" + sql + getUrlAndUser(connection), ex);
    }
    finally {
      SqlUtils.closeQuietly(stmt, connection);
    }
  }

  /**
   * Executes the update using an open connection, and returns the number of rows affected. Use this if you
   * have a connection you want to use again such as one that is autocommit=false
   * 
   * @param connection
   *          conneciton on which to execute the update
   * @param sql
   *          SQL to execute
   * @param name
   *          name of operation (for logging purposes)
   * @return number of rows affected
   * @throws SQLException
   *           if problem executing update
   */
  public static int executeUpdate(Connection connection, String sql, String name) throws SQLException {
    logger.trace("running sql: " + name + "\n" + sql);
    Statement stmt = null;

    try {
      long start = System.currentTimeMillis();
      stmt = connection.createStatement();
      int result = stmt.executeUpdate(sql);
      QueryLogger.logEndStatementExecution(sql, name, start);
      return result;
    }
    catch (SQLException ex) {
      throw new SQLException("Failed to run SQL:\n" + sql + getUrlAndUser(connection), ex);
    }
    finally {
      if (stmt != null)
        stmt.close();
    }
  }

  /**
   * Run a query and returns a resultSet. the calling code is responsible for closing the resultSet using the
   * helper method in SqlUtils.
   * 
   * @param dataSource
   *          data source from which to get connection on which to run query
   * @param sql
   *          SQL to run
   * @param name
   *          name of operation (for logging purposes)
   * @return result set of query
   * @throws SQLException
   *           if problem running query
   */
  public static ResultSet executeQuery(DataSource dataSource, String sql, String name) throws SQLException {
    return executeQuery(dataSource, sql, name, DEFAULT_FETCH_SIZE, false);
  }

  /**
   * Run a query and returns a resultSet. the calling code is responsible for closing the resultSet using the
   * helper method in SqlUtils.
   * 
   * @param dataSource
   *          data source from which to get connection on which to run query
   * @param sql
   *          SQL to run
   * @param name
   *          name of operation (for logging purposes)
   * @param fetchSize
   *          the number of rows to be pre-fetched.
   * @return result set of query
   * @throws SQLException
   *           if problem running query
   */
  public static ResultSet executeQuery(DataSource dataSource, String sql, String name, int fetchSize) throws SQLException {
    return executeQuery(dataSource, sql, name, fetchSize, false);
  }

  /**
   * Run a query and returns a resultSet. the calling code is responsible for closing the resultSet using the
   * helper method in SqlUtils.
   * 
   * @param dataSource
   *          data source from which to get connection on which to run query
   * @param sql
   *          SQL to run
   * @param name
   *          name of operation (for logging purposes)
   * @param fetchSize
   *          the number of rows to be pre-fetched.
   * @param useDBLink
   *          indicates whether this sql uses db_links or not; if db_link is used, a commit will be called
   *          before the sql being executed. This is an Oracle only feature.
   * @return result set of query
   * @throws SQLException
   *           if problem running query
   */
  public static ResultSet executeQuery(DataSource dataSource, String sql, String name, int fetchSize,
      boolean useDBLink) throws SQLException {
    logger.trace("running sql: " + name + "\n" + sql);
    Connection connection = null;
    Statement stmt = null;
    ResultSet resultSet = null;

    try {
      connection = ConnectionMapping.getConnection(dataSource);
      long start = System.currentTimeMillis();
      stmt = connection.createStatement();
      stmt.setFetchSize(fetchSize);
      resultSet = stmt.executeQuery(sql);
      QueryLogger.logStartResultsProcessing(sql, name, start, resultSet);
      return resultSet;
    }
    catch (SQLException ex) {
     if (stmt == null)
        // may or may not have a connection, but don't have stmt or rs
        SqlUtils.closeQuietly(connection);
      else
        // have at least a statement; close everything we can
        closeResultSetAndStatement(resultSet, stmt);
      throw new SQLException("Failed to run query:\n" + sql + getUrlAndUser(connection), ex);
    }
  }

  public static ResultSet executePreparedQuery(PreparedStatement stmt, String sql, String name)
      throws SQLException {
    logger.trace("running sql: " + name + "\n" + sql);
    ResultSet resultSet = null;
    Connection connection = stmt.getConnection();

    try {
      long start = System.currentTimeMillis();
      resultSet = stmt.executeQuery();
      QueryLogger.logStartResultsProcessing(sql, name, start, resultSet);
      return resultSet;
    }
    catch (SQLException ex) {
      closeResultSetAndStatement(resultSet, stmt);
      throw new SQLException("Failed to run query:\n" + sql + getUrlAndUser(connection), ex);
    }
  }

  /**
   * Run the scalar value and returns a single value. If the query returns no rows or more than one row, a
   * WdkModelException will be thrown; if the query returns a single row with many columns, the value in the
   * first column will be returned.
   * 
   * @param dataSource
   *          data source on which to run query
   * @param sql
   *          SQL to run
   * @param name
   *          name of operation (for logging purposes)
   * @return the first column of the first row in the result
   * @throws SQLException
   *           database or query failure
   * @throws IllegalArgumentException
   *           if passed SQL does not return any rows
   */
  public static Object executeScalar(DataSource dataSource, String sql, String name) throws SQLException {
    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(dataSource, sql, name);
      if (!resultSet.next())
        throw new IllegalArgumentException("The SQL doesn't return any row:\n" + sql);
      return resultSet.getObject(1);
    }
    finally {
      closeResultSetAndStatement(resultSet, null);
    }
  }

  public static Set<String> getColumnNames(ResultSet resultSet) throws SQLException {
    Set<String> columns = new LinkedHashSet<String>();
    ResultSetMetaData metaData = resultSet.getMetaData();
    int count = metaData.getColumnCount();
    for (int i = 0; i < count; i++) {
      columns.add(metaData.getColumnName(i));
    }
    return columns;
  }

  /**
   * Escapes the input string for use in LIKE clauses to allow matching special chars
   * 
   * @param value
   *          string on which to operate
   * @return the input value with special characters escaped
   */
  public static String escapeWildcards(String value) {
    return value.replaceAll("%", "{%}").replaceAll("_", "{_}");
  }

  public static void attemptRollback(Connection connection) {
    try {
      connection.rollback();
    }
    catch (SQLException e) {
      logger.error("Could not roll back transaction!", e);
    }
  }

  public static void setClobData(PreparedStatement ps, int columnIndex, Object content) throws SQLException {
    setClobData(ps, columnIndex, content, Types.CLOB);
  }

  public static void setClobData(PreparedStatement ps, int columnIndex, Object content, int charSqlType) throws SQLException {
    if (content == null) {
      ps.setNull(columnIndex, charSqlType);
    }
    else {
      Reader reader = (
          content instanceof Reader ? (Reader)content :
          content instanceof InputStream ? new InputStreamReader((InputStream)content) :
          new StringReader(content.toString()) // convert any other type to String
      );
      ps.setCharacterStream(columnIndex, reader);
    }
  }

  public static void setBinaryData(PreparedStatement ps, int columnIndex, byte[] content, int binarySqlType) throws SQLException {
    if (binarySqlType != Types.BLOB && binarySqlType != Types.LONGVARBINARY) {
      throw new SQLException("BLOB type must be either java.sql.Types.BLOB or java.sql.Types.LONGVARBINARY");
    }
    if (content == null) {
      ps.setNull(columnIndex, binarySqlType);
    }
    else {
      ByteArrayInputStream inStream = new ByteArrayInputStream(content);
      
      switch(binarySqlType) {
        case Types.BLOB:
          ps.setBlob(columnIndex, inStream, content.length);
          break;
        case Types.LONGVARBINARY:
          ps.setBinaryStream(columnIndex, inStream, content.length);
          break;
      }
    }
  }

  /**
   * This method provides "quiet" (i.e. no logging, no exceptions thrown) closing of the following
   * implementations of Wrapper:
   * <ul>
   * <li>{@link java.sql.CallableStatement}</li>
   * <li>{@link java.sql.Connection}</li>
   * <li>{@link java.sql.PreparedStatement}</li>
   * <li>{@link java.sql.ResultSet}</li>
   * <li>{@link java.sql.Statement}</li>
   * <li>{@link org.gusdb.fgputil.db.DatabaseResultStream}</li>
   * <li>{@link org.gusdb.fgputil.db.pool.DatabaseInstance}</li>
   * </ul>
   * 
   * @param wrappers
   *          varargs array of wrappers to be closed
   */
  public static void closeQuietly(Wrapper... wrappers) {
    UncommittedChangesException toThrow = null;
    for (Wrapper wrap : wrappers) {
      if (wrap != null) {
        try {
          if (wrap instanceof DatabaseInstance) {
            ((DatabaseInstance) wrap).close();
          }
          if (wrap instanceof DatabaseResultStream) {
            ((DatabaseResultStream) wrap).close();
          }
          if (wrap instanceof ResultSet) {
            if (!((ResultSet) wrap).isClosed()) ((ResultSet) wrap).close();
          }
          if (wrap instanceof CallableStatement) {
            if (!((CallableStatement) wrap).isClosed()) ((CallableStatement) wrap).close();
          }
          if (wrap instanceof PreparedStatement) {
            if (!((PreparedStatement) wrap).isClosed()) ((PreparedStatement) wrap).close();
          }
          if (wrap instanceof Statement) {
            if (!((Statement) wrap).isClosed()) ((Statement) wrap).close();
          }
          if (wrap instanceof Connection) {
            if (!((Connection) wrap).isClosed()) ((Connection) wrap).close();
          }
        }
        catch (UncommittedChangesException e) {
          // set exception to throw to be the first UncommittedChangesException experienced
          if (toThrow == null)
            toThrow = e;
        }
        catch (Exception e) {
          // ignore all other exceptions
        }
      }
    }
    if (toThrow != null)
      throw toThrow;
  }

  /**
   * Statically bind SQL params to the given statement. This method enables child classes to assign different
   * sets of params multiple times
   * 
   * @param stmt
   *          statement on which to assign params
   * @param types
   *          types of params (values from java.sql.Types)
   * @param args
   *          params to assign
   * @throws SQLException
   *           if error occurs while setting params
   */
  public static void bindParamValues(PreparedStatement stmt, Integer[] types, Object[] args)
      throws SQLException {
    for (int i = 0; i < args.length; i++) {
      if (types == null || types[i] == null) {
        stmt.setObject(i + 1, args[i]);
      }
      else if (args[i] == null) {
        stmt.setNull(i + 1, types[i]);
      }
      // handle arbitrary character data (clob or long varchar)
      else if (types[i].intValue() == Types.CLOB || types[i].intValue() == Types.LONGVARCHAR) {
        setClobData(stmt, i + 1, args[i], types[i]);
      }
      // handle arbitrary binary data (either blob or long byte array)
      else if (types[i].intValue() == Types.BLOB || types[i].intValue() == Types.LONGVARBINARY) {
        setBinaryData(stmt, i + 1, (byte[]) args[i], types[i]);
      }
      else {
        stmt.setObject(i + 1, args[i], types[i]);
      }
    }
  }

  /**
   * Transforms a ResultSet into a Cursor over a stream of typed objects, each created by a row in the
   * ResultSet by the passed object creator
   * 
   * @param rs ResultSet to iterate over
   * @param objCreator function that takes a ResultSet set to a row and creates an object from it
   * @return cursor over the produced objects
   */
  public static <T> Cursor<T> toCursor(final ResultSet rs, final Function<ResultSet,T> objCreator) {
    return new Cursor<T>(){
      @Override
      public boolean next() {
        try {
          return rs.next();
        }
        catch (SQLException e) {
          throw new SqlRuntimeException(e);
        }
      }
      @Override
      public T get() {
        return objCreator.apply(rs);
      }
    };
  }

  /**
   * Performs all the passed procedures in a single transaction on the passed connection.  If any of the
   * passed procedures throw exception, the connection will be rolled back; otherwise all updates will be
   * committed together.  There is no guarantee of rollback of any other connections that may appear in
   * the passed procedures.
   * 
   * @param conn connection on which to perform operations
   * @param procedures set of procedures containing operations (presumably against the passed connection)
   * @throws Exception if any of the procedures fails
   */
  @SafeVarargs
  public static void performInTransaction(Connection conn, ConsumerWithException<Connection>... procedures) throws Exception {
    try {
      conn.setAutoCommit(false);
      for (ConsumerWithException<Connection> proc : procedures) {
        proc.accept(conn);
      }
      // commit the transaction
      conn.commit();
    }
    catch (Exception e) {
      logger.error("Error attempting procedures in a transaction", e);
      try {
        conn.rollback();
      }
      catch (Exception e2) {
        logger.error("Error rolling back transaction", e2);
      }
      throw e;
    }
    finally {
      boolean successfullyResetAutoCommit = false;
      try {
        conn.setAutoCommit(true);
        successfullyResetAutoCommit = true;
        conn.close();
      }
      catch (Exception e) {
        logger.error(successfullyResetAutoCommit ?
            "Error closing connection" : "Error resetting auto-commit = true", e);
      }
    }
  }

  private static String getUrlAndUser(Connection c) {
    String oops = "\nConnect URL: unknown \nUser: unknown";
    if (c == null) return oops;
    try {
      DatabaseMetaData md = c.getMetaData();
      String url = md.getURL();
      String user = md.getUserName();
      return "\nConnect URL: " + url + "\nUser: " + user;
    } catch (Exception e) {
      return oops;
    }
  }

  public static Long fetchNullableLong(ResultSet rs, String columnName, Long nullValue) throws SQLException {
    Long value = rs.getLong(columnName);
    return rs.wasNull() ? nullValue : value;
  }

  public static Integer fetchNullableInteger(ResultSet rs, String columnName, Integer nullValue) throws SQLException {
    Integer value = rs.getInt(columnName);
    return rs.wasNull() ? nullValue : value;
  }

  public static Boolean fetchNullableBoolean(ResultSet rs, String columnName, Boolean nullValue) throws SQLException {
    Boolean value = rs.getBoolean(columnName);
    return rs.wasNull() ? nullValue : value;
  }

  public static void setNullableLong(PreparedStatement ps, int paramIndex, Long value) throws SQLException {
    if (value == null) {
      ps.setNull(paramIndex, Types.BIGINT);
    }
    else {
      ps.setLong(paramIndex, value);
    }
  }

  public static void setNullableString(PreparedStatement ps, int paramIndex, String value) throws SQLException {
    if (value == null) {
      ps.setNull(paramIndex, Types.VARCHAR);
    }
    else {
      ps.setString(paramIndex, value);
    }
  }
}
