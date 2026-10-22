package org.gusdb.fgputil.db.platform;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.postgresql.PGConnection;
import org.postgresql.PGStatement;
import org.postgresql.jdbc.PgResultSet;

public class PostgresFetchConditionsCollector extends DbFetchConditionsCollector {

  private static final Logger LOG = Logger.getLogger(PostgresFetchConditionsCollector.class);

  @Override
  public JSONObject getFetchConditions(PreparedStatement ps, ResultSet rs) throws SQLException {

    PGConnection pgConn = ps.getConnection().unwrap(PGConnection.class);
    PGStatement pgStmt = ps.unwrap(PGStatement.class);

    return super.getFetchConditions(ps, rs)
        .put("defaultFetchSize", pgConn.getDefaultFetchSize())
        .put("preferQueryMode", pgConn.getPreferQueryMode().toString())
        .put("adaptiveFetch", pgStmt.getAdaptiveFetch())
        .put("sql", ps.unwrap(PGStatement.class).toString())
        .put("resultSetInternals", getResultSetInternals(rs));
  }

  /**
   * Returns internal Postgres ResultSet information.  Can be called during a
   * ResultSet processing loop to analyze row buffer size.
   */
  @Override
  public JSONObject getResultSetInternals(ResultSet rs) {
    try {
      Object pgRs = rs.unwrap(PgResultSet.class);
      Class<?> c = pgRs.getClass();

      java.lang.reflect.Field cursorF = c.getDeclaredField("cursor");
      java.lang.reflect.Field rowsF = c.getDeclaredField("rows");
      cursorF.setAccessible(true);
      rowsF.setAccessible(true);

      Object cursor = cursorF.get(pgRs);
      java.util.List<?> rows = (java.util.List<?>) rowsF.get(pgRs);

      return new JSONObject()
          .put("lookupAvailable", true)
          .put("isStreamingAsPortal", (cursor != null))
          .put("numBufferedRows", (rows == null ? 0 : rows.size()));
    }
    catch (Exception e) {
      LOG.warn("Could not dump PG internals", e);
      return new JSONObject()
          .put("lookupAvailable", false);
    }
  }
}
