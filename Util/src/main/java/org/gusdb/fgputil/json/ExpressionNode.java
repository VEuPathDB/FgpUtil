package org.gusdb.fgputil.json;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Parses JSON representing a boolean expression into an object tree that can
 * be converted into SQL or back into JSON. Example JSON:
 * {
 *   "op":"or",
 *   "value:[
 *     {"op":"eq","value":4 },
 *     {"op":"eq","value":8 },
 *     {"op":"and","value":[
 *       {"op":"gt","value":5},
 *       {"op":"lt","value":6}
 *     ]}
 *   ]
 * }
 * 
 * @author rdoherty
 */
public class ExpressionNode {

  public static final String DEFAULT_OPERATOR_KEY = "operator";
  public static final String DEFAULT_VALUE_KEY = "value";

  public static final Function<JsonType,String> STRING_CONVERTER = val -> "'" + val.getString() + "'";
  public static final Function<JsonType,String> NUMBER_CONVERTER = val -> val.get().toString();

  private static final String
    SQL_TODATE_FORMAT  = "YYYY-MM-DD\"T\"HH24:MI:SS\"Z\"",
    JAVA_TODATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";

  public static final Function<JsonType,String> DATE_CONVERTER = val -> {
    String dateStr = val.getString();
    LocalDateTime d = (dateStr.length() <= 10 ? FormatUtil.parseDate(dateStr).atStartOfDay() : FormatUtil.parseDateTime(dateStr));
    String sqlDate = d.format(DateTimeFormatter.ofPattern(JAVA_TODATE_FORMAT));
    return "to_date('" + sqlDate + "', '" + SQL_TODATE_FORMAT + "')";
  };

  public static String toStringSqlExpression(JSONObject json, String columnName) {
    return new ExpressionNode(json, ValueType.STRING).toSqlExpression(columnName, STRING_CONVERTER, true);
  }
  public static String toNumberSqlExpression(JSONObject json, String columnName) {
    return new ExpressionNode(json, ValueType.NUMBER).toSqlExpression(columnName, NUMBER_CONVERTER, true);
  }
  public static String toDateSqlExpression(JSONObject json, String columnName) {
    return new ExpressionNode(json, ValueType.STRING).toSqlExpression(columnName, DATE_CONVERTER, false);
  }

  private enum OperatorType {
    COMBINER,
    UNARY_OPERATOR,
    BINARY_OPERATOR;
  }

  private enum Operator {
    AND("AND", OperatorType.COMBINER),
    OR("OR", OperatorType.COMBINER),
    IS_NULL("IS NULL", OperatorType.UNARY_OPERATOR),
    IS_NOT_NULL("IS NOT NULL", OperatorType.UNARY_OPERATOR),
    EQ("=", OperatorType.BINARY_OPERATOR),
    NE("!=", OperatorType.BINARY_OPERATOR),
    GT(">", OperatorType.BINARY_OPERATOR),
    GE(">=", OperatorType.BINARY_OPERATOR),
    LT("<", OperatorType.BINARY_OPERATOR),
    LE("<=", OperatorType.BINARY_OPERATOR);

    private final String _sqlOperator;
    private final OperatorType _type;

    private Operator(String sqlOperator, OperatorType type) {
      _sqlOperator = sqlOperator;
      _type = type;
    }

    public OperatorType getType() {
      return _type;
    }

    public String getSqlOperator() {
      return _sqlOperator;
    }
  }

  private final String _operatorKey;
  private final String _valueKey;
  private final Operator _operator;
  private final JsonType _rawValue;
  private final List<ExpressionNode> _children;

  public ExpressionNode(JSONObject json, ValueType expectedValueType) throws JSONException {
    this(json, expectedValueType, DEFAULT_OPERATOR_KEY, DEFAULT_VALUE_KEY);
  }

  public ExpressionNode(JSONObject json, ValueType expectedValueType, String operatorKey, String valueKey) throws JSONException {
    Objects.nonNull(json);
    if (!expectedValueType.isTerminal()) {
      throw new IllegalArgumentException("Passed expected value type must be a terminal type.");
    }
    String operatorStr = null;
    try {
      _operatorKey = operatorKey;
      _valueKey = valueKey;
      operatorStr = json.getString(operatorKey);
      _operator = Operator.valueOf(operatorStr.toUpperCase());
      _rawValue = _operator.getType().equals(OperatorType.UNARY_OPERATOR) ?
          null : new JsonType(json.get(valueKey));
      switch(_operator.getType()) {
        case UNARY_OPERATOR:
          // no need to parse value
          _children = null;
          break;
        case BINARY_OPERATOR:
          // check that value is the correct type
          if (!_rawValue.getType().equals(expectedValueType)) {
            throw new JSONException("Value '" + _rawValue + "' is not of expected type (" + expectedValueType + ").");
          }
          _children = null;
          break;
        case COMBINER:
          // value must be an array of nodes
          if (!_rawValue.getType().equals(ValueType.ARRAY)) {
            throw new JSONException("Value of combiner operation must be an array.");
          }
          _children = new ArrayList<>();
          for (JsonType subExpression : JsonIterators.arrayIterable(_rawValue.getJSONArray())) {
            if (!subExpression.getType().equals(ValueType.OBJECT)) {
              throw new JSONException("Child expression of combiner is not an object.");
            }
            _children.add(new ExpressionNode(subExpression.getJSONObject(), expectedValueType, operatorKey, valueKey));
          }
          break;
        default:
          throw new JSONException("Unsupported operator type: " + _operator.getType());
      }
    }
    catch (IllegalArgumentException e) {
      throw new JSONException("Unsupported operator '" + operatorStr + "'.");
    }
  }

  public String toSqlExpression(String column, Function<JsonType,String> valueConverter, boolean optimizeWithInStatement) {
    switch(_operator.getType()) {
      case UNARY_OPERATOR:
        return column + " " + _operator.getSqlOperator();
      case BINARY_OPERATOR:
        return column + " " + _operator.getSqlOperator() + " " + valueConverter.apply(_rawValue);
      case COMBINER:
        return optimizable(_operator, _children) ? getInStatement(column, _children, valueConverter) :
          "( " +
            _children.stream()
              .map(child -> child.toSqlExpression(column, valueConverter, optimizeWithInStatement))
              .collect(Collectors.joining(" " + _operator.getSqlOperator() + " ")) +
          " )";
      default:
        throw new JSONException("Unsupported operator type: " + _operator.getType());
    }
  }

  // already confirmed that all child ops are equals, so just collect the values
  private String getInStatement(String column, List<ExpressionNode> children, Function<JsonType,String> valueConverter) {
    return column + " IN ( " +
      children.stream()
        .map(child -> valueConverter.apply(child._rawValue))
        .collect(Collectors.joining(", ")) +
    " )";
  }

  private boolean optimizable(Operator operator, List<ExpressionNode> nodes) {
    if (!operator.equals(Operator.OR)) {
      return false;
    }
    for (ExpressionNode node : nodes) {
      if (!node._operator.equals(Operator.EQ))
        return false;
    }
    return true;
  }

  public JSONObject toJson() {
    Object value = _rawValue.get();
    return new JSONObject()
      .put(_operatorKey, _operator.name().toLowerCase())
      .put(_valueKey, value.equals(JSONObject.NULL) ? null : value);
  }
}
