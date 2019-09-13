package org.gusdb.fgputil.db.stream;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Wrapper;
import java.util.Iterator;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.ResultSetColumnInfo;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.slowquery.QueryLogger;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.fgputil.iterator.IteratingInputStream;
import org.gusdb.fgputil.iterator.IteratorUtil;

public class ResultSetInputStream extends IteratingInputStream implements Wrapper {

  public interface ResultSetRowConverter {

    byte[] getHeader();

    byte[] getRowDelimiter();

    byte[] getRow(ResultSet resultSet, ResultSetColumnInfo meta) throws SQLException;

    byte[] getFooter();

  }

  /**
   * Creates a ResultSetInputStream by running the passed SQL (uses queryName as
   * name of this query in SQLLogger) against the passed data source and
   * transforming the result using the passed row converter.  If fetchSize
   * passed is greater than zero, applies it to the statement before execution.
   * 
   * @param sql SQL query to run
   * @param queryName name of query (applied in SQLLogger)
   * @param ds data source against which to run query
   * @param fetchSize fetch size to apply (ignored if value is <=0)
   * @param converter row converter to transform data to bytes
   * @return the created stream
   * @throws SQLException if unable to establish connection or run query
   */
  public static ResultSetInputStream getResultSetStream(String sql, String queryName,
      DataSource ds, int fetchSize, ResultSetRowConverter converter) throws SQLException {
    boolean closeDbObjects = false;
    Connection conn = null;
    PreparedStatement stmt = null;
    try {
      long startTime = System.currentTimeMillis();
      conn = ds.getConnection();
      stmt = conn.prepareStatement(sql);
      if (fetchSize > 0) {
        stmt.setFetchSize(fetchSize);
      }
      ResultSet rs = stmt.executeQuery();
      QueryLogger.logStartResultsProcessing(sql, queryName, startTime, rs);
      return new ResultSetInputStream(rs, stmt, conn, converter);
    }
    catch (SQLException e) {
      closeDbObjects = true;
      throw e;
    }
    finally {
      if (closeDbObjects) {
        SqlUtils.closeQuietly(stmt, conn);
      }
    }
  }

  private final ResultSet _rs;
  private final Statement _stmt;
  private final Connection _conn;

  private ResultSetInputStream(ResultSet resultSet, Statement statement, Connection connection,
      ResultSetRowConverter resultConverter) throws SQLException {
    super(buildDataProvider(resultSet, resultConverter));
    _rs = resultSet;
    _stmt = statement;
    _conn = connection;
  }

  private static DataProvider buildDataProvider(ResultSet resultSet,
      ResultSetRowConverter resultConverter) throws SQLException {
    ResultSetColumnInfo columnInfo = new ResultSetColumnInfo(resultSet);
    return new DataProvider() {

      // pass through methods
      @Override public byte[] getHeader()          { return resultConverter.getHeader(); }
      @Override public byte[] getRecordDelimiter() { return resultConverter.getRowDelimiter(); }
      @Override public byte[] getFooter()          { return resultConverter.getFooter(); }

      @Override
      public Iterator<byte[]> getRecordIterator() {
        return IteratorUtil.toIterator(SqlUtils.toCursor(
            resultSet, rs -> Functions.mapException(
                () -> resultConverter.getRow(rs, columnInfo),
                e -> new RuntimeException(e))));
      }
    };
  }

  /**
   * Closes the ResultSet, Statement, and Connection associated with the streamed data.
   */
  @Override
  public void close() throws IOException {
    QueryLogger.logEndResultsProcessing(_rs);
    SqlUtils.closeQuietly(_rs, _stmt, _conn);
  }

  @Override
  public <T> T unwrap(Class<T> iface) throws SQLException {
    throw new UnsupportedOperationException("This class does not wrap an instance of " + iface.getName());
  }

  @Override
  public boolean isWrapperFor(Class<?> iface) throws SQLException {
    return false;
  }
}
