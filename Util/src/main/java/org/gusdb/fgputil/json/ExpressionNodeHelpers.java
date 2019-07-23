package org.gusdb.fgputil.json;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.BiFunction;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.ExpressionNode.Operator;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExpressionNodeHelpers {

  public static final char INCOMING_WILDCARD_CHARACTER = '*';
  
  public static final BiFunction<JsonType,Operator,String> NUMBER_CONVERTER =
    (val, op) -> val.get().toString();

  public static final BiFunction<JsonType,Operator,String> STRING_CONVERTER =
    (val, op) -> {
      String safeString = val.getString().replace("'", "''");
      return "'" + (!op.equals(Operator.LIKE) ? safeString :
        safeString.replace(INCOMING_WILDCARD_CHARACTER, '%')) + "'";
    };

  public static final BiFunction<JsonType, Operator, Operator> LIKE_CONVERTER =
    (val, op) -> {
      boolean hasWildcards = val.getString().indexOf(INCOMING_WILDCARD_CHARACTER) > -1;
      return (op.equals(Operator.LIKE) && !hasWildcards) ? Operator.EQ : op;
    };

  private static final String
    SQL_TODATE_FORMAT  = "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"",
    JAVA_TODATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  public static final BiFunction<JsonType,Operator,String> DATE_CONVERTER =
    (val, op) -> {
      String dateStr = val.getString();
      LocalDateTime d = (dateStr.length() <= 10 ? FormatUtil.parseDate(dateStr).atStartOfDay() : FormatUtil.parseDateTime(dateStr));
      String sqlDate = d.format(DateTimeFormatter.ofPattern(JAVA_TODATE_FORMAT));
      return "to_date('" + sqlDate + "', '" + SQL_TODATE_FORMAT + "')";
    };

  public static String toStringSqlExpression(JSONObject json, String columnName) {
    return new ExpressionNode(json, ValueType.STRING).toSqlExpression(columnName, true, STRING_CONVERTER, LIKE_CONVERTER);
  }

  public static String toNumberSqlExpression(JSONObject json, String columnName) {
    return new ExpressionNode(json, ValueType.NUMBER).toSqlExpression(columnName, true, NUMBER_CONVERTER);
  }

  public static String toDateSqlExpression(JSONObject json, String columnName) {
    return new ExpressionNode(json, ValueType.STRING).toSqlExpression(columnName, false, DATE_CONVERTER);
  }

  public static JSONObject transformToFlatEnumExpression(JSONArray json, String operatorKey, String valueKey) {
    JSONArray subExpressions = new JSONArray();
    for (JsonType value : JsonIterators.arrayIterable(json)) {
      if (!value.getType().isTerminal()) {
        throw new JSONException("Cannot transform JSON array with depth > 1 to enum expression.");
      }
      subExpressions.put(new JSONObject()
        .put(operatorKey, Operator.EQ.name().toLowerCase())
        .put(valueKey, value.get())
      );
    }
    return new JSONObject()
      .put(operatorKey, Operator.OR.name().toLowerCase())
      .put(valueKey, subExpressions);
  }
}
