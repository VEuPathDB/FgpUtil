package org.gusdb.fgputil.json;

import org.gusdb.fgputil.json.JsonType.ValueType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class ExpressionNodeTest {

  private static final String TEST_COLUMN_NAME = "my_column_val";

  private static final JSONObject TEST_JSON_OBJ =
    new JSONObject()
     .put("op","or")
     .put("value", new JSONArray()
       .put(new JSONObject()
         .put("op","eq")
         .put("value",4))
       .put(new JSONObject()
         .put("op","ne")
         .put("value",8))
       .put(new JSONObject()
         .put("op","and")
         .put("value", new JSONArray()
           .put(new JSONObject()
             .put("op","gt")
             .put("value",5))
           .put(new JSONObject()
             .put("op","le")
             .put("value",6))
           .put(new JSONObject()
             .put("op", "is_not_null"))))
       .put(new JSONObject()
         .put("op","or")
         .put("value", new JSONArray()
           .put(new JSONObject()
             .put("op","eq")
             .put("value",13))
           .put(new JSONObject()
             .put("op","eq")
             .put("value",17))
           .put(new JSONObject()
             .put("op","eq")
             .put("value",19)))));

  private static final JSONArray TEST_JSON_ARRAY =
    new JSONArray().put(1).put(2).put(3).put(4).put(5);

  private static final JSONObject TEST_LIKE_WITH_WILDCARDS_JSON =
    new JSONObject()
      .put("operator", "like")
      .put("value", "*a'bc*def");

  private static final JSONObject TEST_LIKE_WITHOUT_WILDCARDS_JSON =
    new JSONObject()
      .put("operator", "like")
      .put("value", "abcdef");

  @Test
  public void doSqlFormatTest() {
    System.out.println("\n*** Format Test ***\n");
    ExpressionNode node = new ExpressionNode(TEST_JSON_OBJ, ValueType.NUMBER, "op", "value");
    System.out.println(node.toSqlExpression(TEST_COLUMN_NAME, true, (json,op) -> json.toString()));
    System.out.println(node.toJson().toString(2));
  }

  @Test
  public void doArrayConversionTest() {
    System.out.println("\n*** Array Conversion Test ***\n");
    System.out.println(TEST_JSON_ARRAY);
    JSONObject objVersion = ExpressionNodeHelpers.transformToFlatEnumExpression(
        TEST_JSON_ARRAY, ExpressionNode.DEFAULT_OPERATOR_KEY, ExpressionNode.DEFAULT_VALUE_KEY);
    System.out.println(objVersion);
    System.out.println(ExpressionNodeHelpers.toNumberSqlExpression(objVersion, TEST_COLUMN_NAME));
  }

  @Test
  public void doLikeTest() {
    System.out.println("\n*** Like Test ***\n");
    System.out.println(TEST_LIKE_WITH_WILDCARDS_JSON);
    System.out.println(ExpressionNodeHelpers.toStringSqlExpression(TEST_LIKE_WITH_WILDCARDS_JSON, TEST_COLUMN_NAME));
    System.out.println(TEST_LIKE_WITHOUT_WILDCARDS_JSON);
    System.out.println(ExpressionNodeHelpers.toStringSqlExpression(TEST_LIKE_WITHOUT_WILDCARDS_JSON, TEST_COLUMN_NAME));
  }

}
