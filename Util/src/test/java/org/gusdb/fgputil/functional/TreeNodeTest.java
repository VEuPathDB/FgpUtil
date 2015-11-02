package org.gusdb.fgputil.functional;

import java.util.List;

import static org.junit.Assert.assertEquals;

import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

public class TreeNodeTest {

  private static Integer[] childVals = { 2, 5, 8 };

  @Test
  public void testStructureMap() {
    TreeNode<Integer> root = buildTestTree();
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
    // since JSON toString() may produce hash values in an inconsistent order, need to build string ourselves
    JSONObject expected = buildExpectedJson();
    System.out.println("Produced: " + json.toString(2));
    assertEquals(toString(expected), toString(json));
  }

  private TreeNode<Integer> buildTestTree() {
    TreeNode<Integer> root = new TreeNode<>(1);
    for (Integer childVal : childVals) {
      TreeNode<Integer> child = new TreeNode<>(childVal);
      child.addChild(childVal + 1);
      child.addChild(childVal + 2);
      root.addChildNode(child);
    }
    return root;
  }

  private JSONObject buildExpectedJson() {
    JSONObject parent = new JSONObject();
    parent.put("value", 1);
    JSONArray children = new JSONArray();
    for (Integer childVal : childVals) {
      JSONObject child = new JSONObject();
      child.put("value", childVal);
      JSONArray grandchildren = new JSONArray();
      for (int i = 1; i <= 2; i++) {
        JSONObject grandchild = new JSONObject();
        grandchild.put("value", childVal + i);
        grandchildren.put(grandchild);
      }
      child.put("children", grandchildren);
      children.put(child);
    }
    parent.put("children", children);
    return parent;
  }

  private String toString(JSONObject json) {
    StringBuilder str = new StringBuilder("{")
      .append("value:").append(json.getInt("value"));
    if (json.has("children")) {
      str.append(",children:[");
      JSONArray children = json.getJSONArray("children");
      for (int i = 0; i < children.length(); i++) {
        if (i > 0) str.append(",");
        str.append(toString(children.getJSONObject(i)));
      }
      str.append("]");
    }
    return str.append("}").toString();
  }
}
