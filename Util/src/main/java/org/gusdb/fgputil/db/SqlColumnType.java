package org.gusdb.fgputil.db;

import java.sql.Types;

/**
 * Defines a set of basic column types and a mapping from JDBC SQL type to
 * those column types.
 * 
 * @author rdoherty
 */
public enum SqlColumnType {
  STRING,
  NUMBER,
  DATE,
  OTHER;

  public static SqlColumnType getFromSqlType(int sqlType) {
    switch(sqlType) {
      case Types.BIT:
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:
      case Types.BIGINT:
      case Types.FLOAT:
      case Types.REAL:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.DECIMAL:
        return NUMBER;

      case Types.CHAR:
      case Types.VARCHAR:
      case Types.LONGVARCHAR:
      case Types.NCHAR:
      case Types.NVARCHAR:
      case Types.LONGNVARCHAR:
      case Types.CLOB:
      case Types.NCLOB:
        return STRING;
      
      case Types.DATE:
      case Types.TIMESTAMP:
      case Types.TIMESTAMP_WITH_TIMEZONE:
        return DATE;

      case Types.BINARY:
      case Types.VARBINARY:
      case Types.LONGVARBINARY:
      case Types.NULL:
      case Types.OTHER:
      case Types.JAVA_OBJECT:
      case Types.DISTINCT:
      case Types.STRUCT:
      case Types.ARRAY:
      case Types.BLOB:
      case Types.REF:
      case Types.DATALINK:
      case Types.BOOLEAN:
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
