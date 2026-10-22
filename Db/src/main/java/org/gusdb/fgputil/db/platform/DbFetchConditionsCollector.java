package org.gusdb.fgputil.db.platform;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.json.JSONObject;

public class DbFetchConditionsCollector {

  public JSONObject getFetchConditions(PreparedStatement ps, ResultSet rs) throws SQLException {
    return new JSONObject()
        .put("autoCommit", ps.getConnection().getAutoCommit())
        .put("statementFetchSize", ps.getFetchSize())
        .put("resultSetFetchSize", rs.getFetchSize())
        .put("resultSetType", getResultSetTypeName(rs.getType()))
        .put("resultSetConcurrency", (rs.getConcurrency() == ResultSet.CONCUR_READ_ONLY ? "READ_ONLY" : "UPDATABLE"))
        .put("resultSetHoldability", getHoldabilityName(ps.getResultSetHoldability()));
  }

  private static String getHoldabilityName(int rsHoldability) {
    switch(rsHoldability) {
      case ResultSet.HOLD_CURSORS_OVER_COMMIT: return "HOLD_CURSORS_OVER_COMMIT";
      case ResultSet.CLOSE_CURSORS_AT_COMMIT:  return "CLOSE_CURSORS_AT_COMMIT";
      default: return String.valueOf(rsHoldability);
    }
  }

  private static String getResultSetTypeName(int rsType) {
    switch (rsType) {
      case ResultSet.TYPE_FORWARD_ONLY:       return "FORWARD_ONLY";
      case ResultSet.TYPE_SCROLL_INSENSITIVE: return "SCROLL_INSENSITIVE";
      case ResultSet.TYPE_SCROLL_SENSITIVE:   return "SCROLL_SENSITIVE";
      default: return String.valueOf(rsType);
    }
  }

  public JSONObject getResultSetInternals(ResultSet rs) {
    // all ResultSet internals are platform-specific
    return new JSONObject();
  }
}
