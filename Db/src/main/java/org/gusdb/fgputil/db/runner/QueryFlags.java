package org.gusdb.fgputil.db.runner;

/**
 * Encapsulates settings to be used when executing database queries.
 */
public class QueryFlags {

  public static final int NO_FETCH_SIZE_OVERRIDE = -1;

  public enum CommitAndClose {
    /**
     * SQLRunner will perform on internally fetched connections;
     * otherwise caller is responsible.
     */
    NORMAL,
    /**
     * Caller is responsible for performing any desired commits, and
     * closing the resulting result set, statement, and connection.
     */
    CALLER_IS_RESPONSIBLE;
  }

  private int _fetchSize = NO_FETCH_SIZE_OVERRIDE;
  private CommitAndClose _commitAndCloseFlag = CommitAndClose.NORMAL;

  public QueryFlags() { }

  public QueryFlags setFetchSize(int fetchSize) {
    if (fetchSize >= 0) {
      _fetchSize = fetchSize;
    }
    return this;
  }

  public QueryFlags setCommitAndCloseFlag(CommitAndClose commitAndCloseFlag) {
    _commitAndCloseFlag = commitAndCloseFlag;
    return this;
  }

  public int getFetchSize() {
    return _fetchSize;
  }

  public CommitAndClose getCommitAndCloseFlag() {
    return _commitAndCloseFlag;
  }
}
