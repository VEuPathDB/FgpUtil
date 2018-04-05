package org.gusdb.fgputil.db.cache;

import static org.gusdb.fgputil.functional.Functions.fSwallow;

import javax.sql.DataSource;

import org.gusdb.fgputil.db.runner.SingleLongResultSetHandler;

public class SqlCountCache extends SqlResultCache<Long> {

  public SqlCountCache(DataSource ds) {
    super(ds, fSwallow(rs -> {
      SingleLongResultSetHandler handler = new SingleLongResultSetHandler();
      handler.handleResult(rs);
      return handler.getRetrievedValue();
    }));
  }

}
