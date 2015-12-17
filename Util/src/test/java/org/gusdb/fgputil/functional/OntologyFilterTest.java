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

  // runtime exception to emulate WdkRuntimeException
  private static class WdkRuntimeException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    public WdkRuntimeException(String message) {
      super(message);
    }
  }

  // interface for an ontology node; I think we should use this instead of a straight Map
  private static interface OntologyNode extends Map<String,List<String>> {
    public boolean isCategory();
  }

  // implementation of an ontology node that explicitly declares if it is a category vs. individual
  private static class OntologyNodeImpl extends HashMap<String,List<String>> implements OntologyNode {
    private static final long serialVersionUID = 1L;
    @Override
    public boolean isCategory() {
      return containsKey("isCategory") && get("isCategory").equals("true");
    }
  }

  // NOTE: this predicate is not needed if we have an explicit isCategory()
  //       method on OntologyNode (which I think we should)
  @SuppressWarnings("unused")
  private static final Predicate<OntologyNode> IS_CATEGORY_PREDICATE = new Predicate<OntologyNode>() {
    @Override
    public boolean test(OntologyNode obj) {
      // Logic could be changed to be the logic in the isCategory() method above if we
      //   remove that method and let an ontology node be a simple map
      return obj.isCategory();
    }
  };

  /**
   * Flattens categories in the passed ontology tree that meet some criteria.
   * If a category node passes the predicate, then it will be removed, and its
   * children will be inherited by its parent.  A 'dummy' 
   * 
   * @param root root of the tree to be operated on
   * @param predicate test for whether to remove category
   * @return a "dummy" parent node of the resulting tree or set of trees
   */
  public static TreeNode<OntologyNode> flattenCategories(
      TreeNode<OntologyNode> root, final Predicate<OntologyNode> predicate) {

    // create a dummy parent to inherit the root's children if the root passes the predicate
    OntologyNode dummyNode = new OntologyNodeImpl();
    final TreeNode<OntologyNode> wrapper = new TreeNode<>(dummyNode);
    wrapper.addChildNode(root);

    // create a custom predicate to test categories against; if node passes, it will be removed
    final Predicate<OntologyNode> customPred = new Predicate<OntologyNode>() {
      @Override
      public boolean test(OntologyNode obj) {
        // don't remove wrapper node
        if (obj == wrapper) return false;
        // only remove categories
        if (!obj.isCategory()) return false;
        // use the passed predicate
        return predicate.test(obj);
      }
    };

    // use a structure mapper to flatten the tree; removed nodes' children will be added to their respective parents
    return root.mapStructure(new StructureMapper<OntologyNode, TreeNode<OntologyNode>>() {
      @Override
      public TreeNode<OntologyNode> map(OntologyNode obj, List<TreeNode<OntologyNode>> mappedChildren) {
        // need to test each child to see if it should be removed, then inherit its children if it should
        TreeNode<OntologyNode> replacement = new TreeNode<>(obj);
        for (TreeNode<OntologyNode> child : mappedChildren) {
          if (customPred.test(child.getContents())) {
            // child will be removed; inherit child node's children
            for (TreeNode<OntologyNode> grandchild : child.getChildNodes()) {
              replacement.addChildNode(grandchild);
            }
          }
          else {
            // child should not be removed; add to replacement's children
            replacement.addChildNode(child);
          }
        }
        return replacement;
      }
    });
  }

  /**
   * This method will, given an ontology tree, do the following:
   * 
   * 1. Clone the tree (original tree will not be modified)
   * 2. Trim any non-category (leaf) nodes based on the passed predicate
   * 3. Remove category nodes that have only one child and add that child to the removed node's parent
   * 
   * @param root root of the tree to be operated on
   * @param predicate test for whether to retain non-category (leaf) nodes
   * @return cloned tree with modifications as above
   */
  public static TreeNode<OntologyNode> getFilteredOntology(
      TreeNode<OntologyNode> root, final Predicate<OntologyNode> predicate) {
    return root.mapStructure(new StructureMapper<OntologyNode, TreeNode<OntologyNode>>() {
      @Override
      public TreeNode<OntologyNode> map(OntologyNode obj, List<TreeNode<OntologyNode>> mappedChildren) {
        if (!obj.isCategory()) {
          if (!mappedChildren.isEmpty()) {
            // Case 1: if I am not a category but I have children, throw exception
            throw new WdkRuntimeException("Individuals should not have children.");
          }
          else if (!predicate.test(obj)) {
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
            return createNodeAndApplyChildren(onlyChild.getContents(), onlyChild.getChildNodes());
          }
          // Case 6: category node with >1 children; return it as is
          else {
            return createNodeAndApplyChildren(obj, mappedChildren);
          }
        }
      }
    });
  }

  private static TreeNode<OntologyNode> createNodeAndApplyChildren(OntologyNode obj,
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
