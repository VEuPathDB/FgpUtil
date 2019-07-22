package org.gusdb.fgputil.json;

import org.gusdb.fgputil.json.JsonType.ValueType;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class ExpressionNodeTest {

  private static final JSONObject TEST_JSON =
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

  @Test
  public void doTest() {
    System.out.println(new ExpressionNode(TEST_JSON, ValueType.NUMBER, "op", "value").toSqlExpression("my_column_val", json -> json.toString(), true));
  }
}
