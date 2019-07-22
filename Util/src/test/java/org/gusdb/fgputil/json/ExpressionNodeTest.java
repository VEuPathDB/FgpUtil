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

  @Test
  public void doSqlFormatTest() {
    System.out.println(new ExpressionNode(TEST_JSON_OBJ, ValueType.NUMBER, "op", "value")
        .toSqlExpression(TEST_COLUMN_NAME, json -> json.toString(), true));
  }

  @Test
  public void doArrayConversionTest() {
    System.out.println(TEST_JSON_ARRAY);
    JSONObject objVersion = ExpressionNode.transformToFlatEnumExpression(
        TEST_JSON_ARRAY, ExpressionNode.DEFAULT_OPERATOR_KEY, ExpressionNode.DEFAULT_VALUE_KEY);
    System.out.println(objVersion);
    System.out.println(ExpressionNode.toNumberSqlExpression(objVersion, TEST_COLUMN_NAME));
  }
}
