package org.gusdb.fgputil.db.leakmonitor;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.ResultSet;

public class CloseableObjectType<T> {

  public static final CloseableObjectType<Connection> Connection = new CloseableObjectType<>(Connection.class);
  public static final CloseableObjectType<Statement> Statement = new CloseableObjectType<>(Statement.class);
  public static final CloseableObjectType<PreparedStatement> PreparedStatement = new CloseableObjectType<>(PreparedStatement.class);
  public static final CloseableObjectType<CallableStatement> CallableStatement = new CloseableObjectType<>(CallableStatement.class);
  public static final CloseableObjectType<ResultSet> ResultSet = new CloseableObjectType<>(ResultSet.class);

  public static CloseableObjectType<?>[] values() {
    return new CloseableObjectType<?>[] {
      Connection, Statement, PreparedStatement, CallableStatement, ResultSet
    };
  }

  private final Class<T> _typeClass;

  private CloseableObjectType(Class<T> typeClass) {
    _typeClass = typeClass;
  }

  public Class<T> getTypeClass() {
    return _typeClass;
  }

  public String getName() {
    return _typeClass.getSimpleName();
  }

}