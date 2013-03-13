package org.gusdb.fgputil.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Wrapper;

public class SqlUtil {
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
