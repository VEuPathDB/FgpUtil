package org.gusdb.fgputil.functional;

import java.util.List;
import static org.junit.Assert.assertEquals;
import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class TreeNodeTest {

  @Test
  public void testStructureMap() {
    TreeNode<Integer> root = new TreeNode<>(1);
    Integer[] childVals = { 2, 5, 8 };
    for (Integer childVal : childVals) {
      TreeNode<Integer> child = new TreeNode<>(childVal);
      child.addChild(childVal + 1);
      child.addChild(childVal + 2);
      root.addChildNode(child);
    }
    JSONObject json = root.mapStructure(new StructureMapper<Integer, JSONObject>(){
      @Override
      public JSONObject map(Integer obj, List<JSONObject> mappedChildren) {
        JSONObject json = new JSONObject();
        json.put("value", obj);
        if (!mappedChildren.isEmpty()) {
          JSONArray children = new JSONArray();
          for (JSONObject child : mappedChildren) {
            children.put(child);
          }
          json.put("children", children);
        }
        return json;
      }
    });
    System.out.println(json.toString(2));
    String expected = "{\"value\":1,\"children\":[{\"value\":2,\"children\":[{\"value\":3},{\"value\":4}]},{\"value\":5,\"children\":[{\"value\":6},{\"value\":7}]},{\"value\":8,\"children\":[{\"value\":9},{\"value\":10}]}]}";
    assertEquals(expected, json.toString());
  }
}
