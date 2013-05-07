package org.gusdb.fgputil.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;

/**
 * @author Jerric Gao
 */
public final class SqlUtils {

  private static final Logger logger = Logger.getLogger(SqlUtils.class.getName());

  /**
   * private constructor, make sure SqlUtils cannot be instanced.
   */
  private SqlUtils() {}
  
  /**
   * Close the resultSet and the underlying statement, connection. Log the query.
   * 
   * @param resultSet
   */
  public static void closeResultSetAndStatement(ResultSet resultSet) {
    try {
      if (resultSet != null) {
        // close the statement in any way
        Statement stmt = null;
        try {
          try {
            stmt = resultSet.getStatement();
          } finally {
            closeResultSetOnly(resultSet);
          }
        } finally {
          closeStatement(stmt);
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  /**
   * Close the resultSet but not its statement. Log the query.
   * 
   * @param resultSet
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

    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * Close the statement and underlying connection
   * 
   * @param stmt
   * @throws SQLException
   */
  public static void closeStatement(Statement stmt) {
    try {
      if (stmt != null) {
        // close the connection in any way
        Connection connection = null;
        try {
          try {
            connection = stmt.getConnection();
          } finally {
            stmt.close();
          }
        } finally {
          if (connection != null)
            connection.close();
        }
      }
    } catch (SQLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static PreparedStatement getPreparedStatement(DataSource dataSource,
      String sql) throws SQLException {
    Connection connection = null;
    PreparedStatement ps = null;
    try {
      connection = dataSource.getConnection();
      ps = connection.prepareStatement(sql);
      ps.setFetchSize(100);
      return ps;
    } catch (SQLException ex) {
      logger.error("Failed to prepare query:\n" + sql);
      closeStatement(ps);

      if (ps == null && connection != null)
        SqlUtils.closeQuietly(connection);
      throw ex;
    }
  }

  /**
   * execute the update, and returns the number of rows affected.
   * 
   * @param dataSource
   * @param sql
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static boolean executePreparedStatement(PreparedStatement stmt,
      String sql, String name) throws SQLException {
    try {
      long start = System.currentTimeMillis();
      boolean result = stmt.execute();
      QueryLogger.logEndStatementExecution(sql, name, start);
      return result;
    } catch (SQLException ex) {
      logger.error("Failed to execute statement: \n" + sql);
      throw ex;
    }
  }

  /**
   * execute the update, and returns the number of rows affected.
   * 
   * @param dataSource
   * @param sql
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static int executeUpdate(DataSource dataSource,
      String sql, String name) throws SQLException {
    Connection connection = null;
    Statement stmt = null;
    try {
      long start = System.currentTimeMillis();
      connection = dataSource.getConnection();
      stmt = connection.createStatement();
      int result = stmt.executeUpdate(sql);
      QueryLogger.logEndStatementExecution(sql, name, start);
      return result;
    } catch (SQLException ex) {
      logger.error("Failed to run nonQuery:\n" + sql);
      throw ex;
    } finally {
      closeStatement(stmt);
      if (stmt == null && connection != null)
        SqlUtils.closeQuietly(connection);
    }
  }

  /**
   * execute the update using an open connection, and returns the number of rows
   * affected. Use this if you have a connection you want to use again such as
   * one that is autocommit=false
   * 
   * @param connection
   * @param sql
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static int executeUpdate(Connection connection,
      String sql, String name) throws SQLException {
    Statement stmt = null;
    try {
      long start = System.currentTimeMillis();
      stmt = connection.createStatement();
      int result = stmt.executeUpdate(sql);
      QueryLogger.logEndStatementExecution(sql, name, start);
      return result;
    } catch (SQLException ex) {
      logger.error("Failed to run nonQuery:\n" + sql);
      throw ex;
    } finally {
      if (stmt != null)
        stmt.close();
    }
  }

  /**
   * Run a query and returns a resultSet. the calling code is responsible for
   * closing the resultSet using the helper method in SqlUtils.
   * 
   * @param dataSource
   * @param sql
   * @return
   * @throws SQLException
   * @throws WdkModelException
   * @throws WdkUserException
   */
  public static ResultSet executeQuery(DataSource dataSource, String sql, String name)
      throws SQLException {
    return executeQuery(dataSource, sql, name, 100);
  }

  public static ResultSet executeQuery(DataSource dataSource, String sql, String name, int fetchSize)
      throws SQLException {
    ResultSet resultSet = null;
    Connection connection = null;
    try {
      long start = System.currentTimeMillis();
      connection = dataSource.getConnection();
      Statement stmt = connection.createStatement();
      stmt.setFetchSize(fetchSize);
      resultSet = stmt.executeQuery(sql);
      QueryLogger.logStartResultsProcessing(sql, name, start, resultSet);
      return resultSet;
    } catch (SQLException ex) {
      logger.error("Failed to run query:\n" + sql);
      if (resultSet == null && connection != null)
        SqlUtils.closeQuietly(connection);
      closeResultSetAndStatement(resultSet);
      throw ex;
    }
  }

  /**
   * Run the scalar value and returns a single value. If the query returns no
   * rows or more than one row, a WdkModelException will be thrown; if the query
   * returns a single row with many columns, the value in the first column will
   * be returned.
   * 
   * @param dataSource
   * @param sql
   * @return the first column of the first row in the result
   * @throws SQLException
   *           database or query failure
   */
  public static Object executeScalar(DataSource dataSource,
      String sql, String name) throws SQLException, DBStateException {
    ResultSet resultSet = null;
    try {
      resultSet = executeQuery(dataSource, sql, name);
      if (!resultSet.next())
        throw new DBStateException("The SQL doesn't return any row:\n" + sql);
      return resultSet.getObject(1);
    } finally {
      closeResultSetAndStatement(resultSet);
    }
  }

  public static Set<String> getColumnNames(ResultSet resultSet)
      throws SQLException {
    Set<String> columns = new LinkedHashSet<String>();
    ResultSetMetaData metaData = resultSet.getMetaData();
    int count = metaData.getColumnCount();
    for (int i = 0; i < count; i++) {
      columns.add(metaData.getColumnName(i));
    }
    return columns;
  }

  /**
   * Escapes the input string for use in LIKE clauses to allow matching special
   * chars
   * 
   * @param value
   * @return the input value with special characters escaped
   */
  public static String escapeWildcards(String value) {
    return value.replaceAll("%", "{%}").replaceAll("_", "{_}");
  }

  public static void attemptRollback(Connection connection) {
    try {
      connection.rollback();
    } catch (SQLException e) {
      logger.error("Could not roll back transaction!", e);
    }
  }
  
  /**
   * This method provides "quiet" (i.e. no logging, no exceptions thrown)
   * closing of the following implementations of Wrapper:
   * <ul>
   *   <li>{@link java.sql.CallableStatement}</li>
   *   <li>{@link java.sql.Connection}</li>
   *   <li>{@link java.sql.PreparedStatement}</li>
   *   <li>{@link java.sql.ResultSet}</li>
   *   <li>{@link java.sql.Statement}</li>
   *   <li>{@link org.gusdb.fgputil.db.DatabaseResultStream}</li>
   * </ul>
   * 
   * @param wrappers varargs array of wrappers to be closed
   */
  public static void closeQuietly(Wrapper... wrappers) {
    for (Wrapper wrap : wrappers) {
      if (wrap != null) {
        try {
          if (wrap instanceof DatabaseResultStream) {
            ((DatabaseResultStream) wrap).close();
          }
          if (wrap instanceof ResultSet) {
            ((ResultSet) wrap).close();
          }
          if (wrap instanceof CallableStatement) {
            ((CallableStatement) wrap).close();
          }
          if (wrap instanceof PreparedStatement) {
            ((PreparedStatement) wrap).close();
          }
          if (wrap instanceof Statement) {
            ((Statement) wrap).close();
          }
          if (wrap instanceof Connection) {
            ((Connection) wrap).close();
          }
        } catch (Exception e) {}
      }
    }
  }
}
