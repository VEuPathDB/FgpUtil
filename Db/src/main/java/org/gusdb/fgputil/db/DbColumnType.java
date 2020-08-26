package org.gusdb.fgputil.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Arrays;
import java.util.Optional;

/**
 * Defines a set of basic column types and a mapping from JDBC SQL type to
 * those column types.
 *
 * @author rdoherty
 */
public enum DbColumnType {
  BOOLEAN,
  LONG_INT,
  DOUBLE,
  STRING,
  CLOB,
  BINARY_DATA,
  DATE_TIME,
  OTHER;

  public Object getObject(ResultSet resultSet, String columnLabel, DbColumnType type) throws SQLException {
    Object obj = getObjectByName(resultSet, columnLabel, type);
    return resultSet.wasNull() ? null : obj;
  }

  private Object getObjectByName(ResultSet resultSet, String columnLabel, DbColumnType type) throws SQLException {
    switch(type) {
      case BOOLEAN:     return resultSet.getBoolean(columnLabel);
      case LONG_INT:    return resultSet.getLong(columnLabel);
      case DOUBLE:      return resultSet.getDouble(columnLabel);
      case STRING:      return resultSet.getString(columnLabel);
      case CLOB:        return resultSet.getClob(columnLabel);
      case BINARY_DATA: return resultSet.getBlob(columnLabel);
      case DATE_TIME:   return resultSet.getDate(columnLabel);
      case OTHER:       /* pass through to default */
      default:          return resultSet.getObject(columnLabel);
    }
  }

  public Object getObject(ResultSet resultSet, int columnIndex, DbColumnType type) throws SQLException {
    Object obj = getObjectByIndex(resultSet, columnIndex, type);
    return resultSet.wasNull() ? null : obj;
  }

  private Object getObjectByIndex(ResultSet resultSet, int columnIndex, DbColumnType type) throws SQLException {
    switch(type) {
      case LONG_INT:    return resultSet.getLong(columnIndex);
      case DOUBLE:      return resultSet.getDouble(columnIndex);
      case STRING:      return resultSet.getString(columnIndex);
      case CLOB:        return resultSet.getClob(columnIndex);
      case BINARY_DATA: return resultSet.getBlob(columnIndex);
      case DATE_TIME:   return resultSet.getDate(columnIndex);
      case OTHER:       /* pass through to default */
      default:          return resultSet.getObject(columnIndex);
    }
  }

  public static Optional<DbColumnType> fromString(final String val) {
    final String test = val.toUpperCase();
    return Arrays.stream(values())
      .filter(e -> e.name().equals(test))
      .findFirst();
  }

  public static DbColumnType getFromSqlType(int sqlType) {
    switch(sqlType) {

      case Types.BOOLEAN:
      case Types.BIT:
        return BOOLEAN;

      case Types.FLOAT:
      case Types.REAL:
      case Types.DOUBLE:
      case Types.DECIMAL:
        return LONG_INT;

      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.NUMERIC:
        return DOUBLE;

      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
        return STRING;

      case Types.CLOB:
      case Types.NCLOB:
        return CLOB;

      case Types.DATE:
      case Types.TIMESTAMP:
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return DATE_TIME;

      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
      case Types.BLOB:
        return BINARY_DATA;

      case Types.NULL:
      case Types.OTHER:
      case Types.JAVA_OBJECT:
      case Types.DISTINCT:
      case Types.STRUCT:
      case Types.ARRAY:
      case Types.REF:
      case Types.DATALINK:
      case Types.SQLXML:
      case Types.REF_CURSOR:
      case Types.TIME:
      case Types.TIME_WITH_TIMEZONE:
      case Types.ROWID:
      default:
        return OTHER;
    }
  }

}
