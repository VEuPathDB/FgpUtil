package org.gusdb.fgputil.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

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

  private enum OperatorType {
    COMBINER,
    UNARY_OPERATOR,
    BINARY_OPERATOR;
  }

  private static final ValueType[] ALL_TYPES = {};
  private static final ValueType[] STRING_ONLY = { ValueType.STRING };

  public enum Operator {

    AND("AND", OperatorType.COMBINER, ALL_TYPES),
    OR("OR", OperatorType.COMBINER, ALL_TYPES),
    IS_NULL("IS NULL", OperatorType.UNARY_OPERATOR, ALL_TYPES),
    IS_NOT_NULL("IS NOT NULL", OperatorType.UNARY_OPERATOR, ALL_TYPES),
    EQ("=", OperatorType.BINARY_OPERATOR, ALL_TYPES),
    NE("<>", OperatorType.BINARY_OPERATOR, ALL_TYPES),
    GT(">", OperatorType.BINARY_OPERATOR, ALL_TYPES),
    GE(">=", OperatorType.BINARY_OPERATOR, ALL_TYPES),
    LT("<", OperatorType.BINARY_OPERATOR, ALL_TYPES),
    LE("<=", OperatorType.BINARY_OPERATOR, ALL_TYPES),
    LIKE("LIKE", OperatorType.BINARY_OPERATOR, STRING_ONLY);

    private final String _sqlOperator;
    private final OperatorType _type;
    private final Set<ValueType> _allowedValueTypes;

    private Operator(String sqlOperator, OperatorType type, ValueType[] allowedValueTypes) {
      _sqlOperator = sqlOperator;
      _type = type;
      _allowedValueTypes = new HashSet<>(Arrays.asList(allowedValueTypes));
    }

    public OperatorType getType() {
      return _type;
    }

    public String getSqlOperator() {
      return _sqlOperator;
    }

    public boolean supports(ValueType type) {
      return _allowedValueTypes.isEmpty() ? true : _allowedValueTypes.contains(type);
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
      if (!_operator.supports(expectedValueType)) {
        throw new JSONException("Operator " + _operator + " does not support data of type " + expectedValueType);
      }
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
          if (_children.size() < 2) {
            throw new JSONException("Combiner operation '" + _operator + "' must have at least two operands.");
          }
          break;
        default:
          throw new JSONException("Unsupported operator type: " + _operator.getType());
      }
    }
    catch (IllegalArgumentException e) {
      throw new JSONException("Unsupported operator '" + operatorStr + "'.", e);
    }
  }

  public String toSqlExpression(String column, boolean optimizeWithInStatement,
      BiFunction<JsonType, Operator, String> valueConverter) {
    return toSqlExpression(column, optimizeWithInStatement, valueConverter, (type,op) -> op);
  }

  public String toSqlExpression(String column, boolean optimizeWithInStatement,
      BiFunction<JsonType, Operator, String> valueConverter,
      BiFunction<JsonType, Operator, Operator> operatorConverter) {
    Operator operator = operatorConverter.apply(_rawValue, _operator);
    if (!operator.getType().equals(_operator.getType())) {
      throw new IllegalArgumentException("Operator converter function cannot " +
          "change the type of the operator.  Tried to change from " + _operator +
          " (" + _operator.getType() + ") to " + operator + " (" + operator.getType() + ").");
    }
    switch(operator.getType()) {
      case UNARY_OPERATOR:
        return column + " " + operator.getSqlOperator();
      case BINARY_OPERATOR:
        return column + " " + operator.getSqlOperator() + " " + valueConverter.apply(_rawValue, operator);
      case COMBINER:
        return optimizable(operator, _children) ? getInStatement(column, _children, valueConverter) :
          "( " +
            _children.stream()
              .map(child -> child.toSqlExpression(column, optimizeWithInStatement, valueConverter, operatorConverter))
              .collect(Collectors.joining(" " + operator.getSqlOperator() + " ")) +
          " )";
      default:
        throw new JSONException("Unsupported operator type: " + operator.getType());
    }
  }

  // already confirmed that all child ops are equals, so just collect the values
  private String getInStatement(String column, List<ExpressionNode> children,
      BiFunction<JsonType, Operator, String> valueConverter) {
    return column + " IN ( " +
      children.stream()
        .map(child -> valueConverter.apply(child._rawValue, child._operator))
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
