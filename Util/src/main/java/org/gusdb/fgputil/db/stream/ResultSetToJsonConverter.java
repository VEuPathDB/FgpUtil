package org.gusdb.fgputil.db.stream;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.db.DbColumnType;
import org.gusdb.fgputil.db.ResultSetColumnInfo;
import org.gusdb.fgputil.db.stream.ResultSetInputStream.ResultSetRowConverter;
import org.json.JSONObject;

public class ResultSetToJsonConverter implements ResultSetRowConverter {

  @Override
  public byte[] getHeader() {
    return "[".getBytes();
  }

  @Override
  public byte[] getRowDelimiter() {
    return ",".getBytes();
  }

  @Override
  public byte[] getRow(ResultSet rs, ResultSetColumnInfo meta) throws SQLException {
    JSONObject rowJson = new JSONObject();
    for (int i = 1; i <= meta.getColumnCount(); i++) {
      DbColumnType colType = DbColumnType.getFromSqlType(meta.getColumnType(i));
      Object value = colType.getObject(rs, i, colType);
      if (colType.equals(DbColumnType.BINARY_DATA) ||
          colType.equals(DbColumnType.OTHER)) {
        throw new IllegalArgumentException("This converter cannot process BLOB or OTHER column types.");
      }
      else if (colType.equals(DbColumnType.DATE_TIME)) {
        value = new SimpleDateFormat(FormatUtil.STANDARD_DATETIME_FORMAT_DASH).format((Date)value);
        //value = FormatUtil.formatDateTime((Date)value);
      }
      else if (colType.equals(DbColumnType.CLOB)) {
        try {
          StringWriter writer = new StringWriter();
          IoUtil.transferStream(writer, ((Clob)value).getCharacterStream());
          value = writer.toString();
        }
        catch (IOException e) {
          throw new SQLException("Unable to read CLOB value", e);
        }
      }
      rowJson.put(meta.getColumnLabel(i), value);
    }
    return rowJson.toString().getBytes();
  }

  @Override
  public byte[] getFooter() {
    return "]".getBytes();
  }
}
