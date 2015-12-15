package org.gusdb.fgputil.functional;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.fgputil.ListBuilder;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.TreeNode.StructureMapper;
import org.junit.Test;

/**
 * Contains an implementation of an ontology filtering method that not only
 * prune leaves that match a predicate but also "flatten" branches with only one
 * child and remove branches that are left with no individuals after pruning.
 * 
 * This is to support our new ontology framework for object categorization.
 * 
 * @author rdoherty
 */
public class OntologyFilterTest {

  private static class WdkRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public WdkRuntimeException(String message) {
      super(message);
    }
  }

  private static interface OntologyNode extends Map<String,List<String>> {
    public boolean isCategory();
  }

  private static class OntologyNodeImpl extends HashMap<String,List<String>> implements OntologyNode {
    private static final long serialVersionUID = 1L;
    @Override
    public boolean isCategory() {
      return containsKey("isCategory") && get("isCategory").equals("true");
    }
  }

  // NOTE: this predicate is not needed if we have an explicit isCategory() method on OntologyNode (which I think we should)
  @SuppressWarnings("unused")
  private static final Predicate<OntologyNode> IS_CATEGORY_PREDICATE = new Predicate<OntologyNode>() {
    @Override
    public boolean test(OntologyNode obj) {
      // could be changed to be the logic in the isCategory() method above if we
      //   remove that method and let an ontology node be a simple map
      return obj.isCategory();
    }
  };

  // Ryan's solution
  public TreeNode<OntologyNode> getFilteredOntology(
      TreeNode<OntologyNode> root, final Predicate<OntologyNode> nodePred) {
    return root.mapStructure(new StructureMapper<OntologyNode, TreeNode<OntologyNode>>() {
      @Override
      public TreeNode<OntologyNode> map(OntologyNode obj, List<TreeNode<OntologyNode>> mappedChildren) {
        if (!obj.isCategory()) {
          if (!mappedChildren.isEmpty()) {
            // Case 1: if I am not a category but I have children, throw exception
            throw new WdkRuntimeException("Individuals should not have children.");
          }
          else if (!nodePred.test(obj)) {
            // Case 2: if I am not a category but don't pass the predicate, return null
            return null;
          }
          else {
            // Case 3: if I am not a category and pass the predicate, return clone of leaf
            return new TreeNode<OntologyNode>(obj);
          }
        }
        else { // this node is a category
          trimNulls(mappedChildren);
          // Case 4: if I have 0 non-null children, return null
          if (mappedChildren.isEmpty()) {
            return null;
          }
          // Case 5: if I have 1 non-null child, replace my contents with theirs and return me
          else if (mappedChildren.size() == 1) {
            TreeNode<OntologyNode> onlyChild = mappedChildren.get(0);
            return cloneAndApplyChildren(onlyChild.getContents(), onlyChild.getChildNodes());
          }
          // Case 6: category node with >1 children; return it as is
          else {
            return cloneAndApplyChildren(obj, mappedChildren);
          }
        }
      }
    });
  }

  private static TreeNode<OntologyNode> cloneAndApplyChildren(OntologyNode obj,
      List<TreeNode<OntologyNode>> children) {
    TreeNode<OntologyNode> clone = new TreeNode<OntologyNode>(obj);
    for (TreeNode<OntologyNode> child : children) {
      clone.addChildNode(child);
    }
    return clone;
  }

   private static void trimNulls(List<?> list) {
     for (int i = 0; i < list.size(); i++) {
       if (list.get(i) == null) list.remove(i);
       i--;
     }
   }

  @Test
  public void testOntologyUsage() {
    // dummy values
    final String KEY = "property name";
    final String VALUE = "a value that should be kept";

    // dummy objects
    OntologyNode rootContent = new OntologyNodeImpl();
    rootContent.put(KEY, new ListBuilder<String>(VALUE).toList());
    TreeNode<OntologyNode> masterOntology = new TreeNode<>(rootContent);

    // What we want is to define a predicate on individuals, trim the tree based
    // on the predicate, then "flatten" the tree so that any categories that no
    // longer have individuals are removed (recursively), and any categories
    // that only have one individual are removed, passing their individual to
    // their parent (also recursively).

    final Predicate<OntologyNode> TEST_PREDICATE = new Predicate<OntologyNode>() {
      @Override
      public boolean test(OntologyNode obj) {
        List<String> values = obj.get(KEY);
        return values != null && values.contains(VALUE);
      }
    };

    // will remove leaf (non-category) nodes that don't pass the predicate's test
    TreeNode<OntologyNode> filteredOntology = getFilteredOntology(masterOntology, TEST_PREDICATE);

     assertNotNull(filteredOntology);
  }
}
