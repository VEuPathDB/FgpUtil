package org.gusdb.fgputil.json;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.function.BiFunction;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.ExpressionNode.Operator;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ExpressionNodeHelpers {

  private ExpressionNodeHelpers(){} // cannot instantiate

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

  public static JSONObject transformFlatEnumConfig(JSONArray json) {
    return transformFlatEnumConfig(json, ExpressionNode.DEFAULT_OPERATOR_KEY, ExpressionNode.DEFAULT_VALUE_KEY);
  }

  /**
   * Transforms enumerated values JSON array into standard expression node syntax, e.g.
   * 
   * [ a, b ] => { "op": "or", "val": [ { "op": "eq", "val": a }, { "op": "eq", "val": b } ] }
   * 
   * @param json enumerated values JSON array
   * @param operatorKey key to which operators should be assigned
   * @param valueKey key to which values should be assigned
   * @return expression node syntax for enumerated values
   */
  public static JSONObject transformFlatEnumConfig(JSONArray json, String operatorKey, String valueKey) throws JSONException {
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

  public static JSONObject transformRangeConfig(JSONObject json) {
    return transformRangeConfig(json, ExpressionNode.DEFAULT_OPERATOR_KEY, ExpressionNode.DEFAULT_VALUE_KEY);
  }

  /**
   * Transforms range JSON into standard expression syntax, e.g.
   *  {
   *    min?: { value: T, isInclusive: boolean },
   *    max?: { value: T, isInclusive: boolean }
   *  }
   * 
   * @param json range json in the format above
   * @param operatorKey key to which operators should be assigned
   * @param valueKey key to which values should be assigned
   * @return expression node syntax for a range
   */
  public static JSONObject transformRangeConfig(JSONObject json, String operatorKey, String valueKey) throws JSONException {
    Optional<JSONObject> minNode = parseRangeBoundary(json, "min", Operator.GT, Operator.GE, operatorKey, valueKey);
    Optional<JSONObject> maxNode = parseRangeBoundary(json, "max", Operator.LT, Operator.LE, operatorKey, valueKey);
    if (minNode.isEmpty() && maxNode.isEmpty()) {
      throw new JSONException("Range configuration must contain 'min' or 'max' or both.");
    }
    return (minNode.isPresent() && maxNode.isPresent()) ?
      new JSONObject()
        .put(operatorKey, Operator.AND.name().toLowerCase())
        .put(valueKey, new JSONArray().put(minNode.get()).put(maxNode.get())) :
      minNode.orElse(maxNode.get());
  }

  private static Optional<JSONObject> parseRangeBoundary(JSONObject json,
      String boundaryKey, Operator exclusiveOp, Operator inclusiveOp,
      String operatorKey, String valueKey) {
    if (!json.has(boundaryKey)) return Optional.empty();
    JSONObject boundaryJson = json.getJSONObject(boundaryKey);
    Operator op = boundaryJson.getBoolean("isInclusive") ? inclusiveOp : exclusiveOp;
    return Optional.of(new JSONObject()
        .put(operatorKey, op.name().toLowerCase())
        .put(valueKey, boundaryJson.get("value")));
  }

  public static JSONObject transformPatternConfig(String value) {
    return transformPatternConfig(value, ExpressionNode.DEFAULT_OPERATOR_KEY, ExpressionNode.DEFAULT_VALUE_KEY);
  }

  /**
   * Transforms passed pattern into standard expression syntax for a LIKE restraint, e.g.
   * 
   *  "value" -> { "op": "like", "val": "value" }
   * 
   * @param value pattern sent in pattern request
   * @param operatorKey key to which operators should be assigned
   * @param valueKey key to which values should be assigned
   * @return expression node syntax for a like constraint
   */
  public static JSONObject transformPatternConfig(String value, String operatorKey, String valueKey) throws JSONException {
    return new JSONObject()
        .put(operatorKey, Operator.LIKE.name().toLowerCase())
        .put(valueKey, value);
  }
}
