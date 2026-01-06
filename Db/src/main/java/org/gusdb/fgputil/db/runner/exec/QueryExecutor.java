package org.gusdb.fgputil.db.runner.exec;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.db.SqlUtils;
import org.gusdb.fgputil.db.runner.QueryFlags;
import org.gusdb.fgputil.db.runner.QueryFlags.CommitAndClose;
import org.gusdb.fgputil.db.runner.handler.ResultSetHandler;
import org.gusdb.fgputil.db.runner.SQLRunnerException;

/**
 * Executor for SQL queries.  This executor requires the implementation of a
 * ResultSetHandler to process the results of the query.
 * 
 * @author rdoherty
 */
public class QueryExecutor<T> extends PreparedStatementExecutor<T> {

  private static final Logger LOG = Logger.getLogger(QueryExecutor.class);

  private QueryFlags _queryFlags;
  private ResultSetHandler<T> _handler;
  private ResultSet _results;

  public QueryExecutor(QueryFlags queryFlags, Object[] args, Integer[] types, ResultSetHandler<T> handler) {
    super(args, types);
    _queryFlags = queryFlags;
    _handler = handler;
  }

  @Override
  public void setAutocommit(Connection conn) throws SQLException {
    conn.setAutoCommit(false);
  }

  @Override
  public void overrideFetchSize(PreparedStatement stmt) throws SQLException {
    if (_queryFlags.getFetchSize() >= 0) {
      stmt.setFetchSize(_queryFlags.getFetchSize());
    }
  }

  @Override
  public void run(PreparedStatement stmt) throws SQLException {
    _results = stmt.executeQuery();
  }

  @Override
  public T handleResult() throws SQLException, SQLRunnerException {
    return _handler.handleResult(_results);
  }

  @Override
  public boolean resourcesShouldBeClosed() {
    return _queryFlags.getCommitAndCloseFlag() == CommitAndClose.NORMAL;
  }

  @Override
  public void closeQuietly() {
    try {
      // clean up any lingering resources on this connection
      _results.getStatement().getConnection().commit();
    }
    catch (Exception e) {
      LOG.warn("Unable to commit on connection after running query", e);
    }
    SqlUtils.closeQuietly(_results);
  }
}
