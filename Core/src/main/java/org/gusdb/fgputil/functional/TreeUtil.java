package org.gusdb.fgputil.functional;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.gusdb.fgputil.Tuples.TwoTuple;

/**
 * Contains static functions (should be "pure") that operate on the TreeNode
 * implementation of trees.
 * 
 * @author rdoherty
 */
// TODO: move utility methods from TreeNode to here and convert to pure functions
public class TreeUtil {

  // class should not be instantiated
  private TreeUtil(){}

  /**
   * Given a tree, returns a copy, trimmed to only nodes of interest (active
   * nodes) and their shared ancestors.  Inactive nodes and parents of only
   * one active node are removed.
   * 
   * Sample usage that may support a trimmed tree for SQL joins:
   *
   * private static class Entity { public String getName() { return null; }}
   * TreeNode&lt;Entity&gt; entityTree;
   * String targetEntityName;
   * List&lt;String&gt; filteredEntityNames;
   *
   * TreeNode&lt;Entity&gt; trimmedRoot = trimToActiveAndPivotNodes(entityTree,
   *   e -&gt; targetEntityName.equals(e.getName()) || filteredEntityNames.contains(e.getName()));
   */
  public static <T> TreeNode<T> trimToActiveAndPivotNodes(TreeNode<T> root, Predicate<T> isActive) {
    return root.mapStructure((nodeContents, mappedChildren) -> {
      List<TreeNode<T>> activeChildren = mappedChildren.stream()
          .filter(child -> child != null) // filter dead branches
          .collect(Collectors.toList());
      return isActive.test(nodeContents) || activeChildren.size() > 1 ?
        // this node is active itself or a pivot node; return with any active children
        new TreeNode<T>(nodeContents).addAllChildNodes(activeChildren) :
        // inactive, non-pivot node; return single active child or null
        activeChildren.isEmpty() ? null : activeChildren.get(0);
    });
  }

  /**
   * Returns a list of tuples, each representing an edge by containing the
   * content of the nodes joined by the edge.
   *
   * @param <T> type of node contents
   * @param root root node of the tree
   * @return list of edges i.e. node tuples
   */
  @SuppressWarnings("unchecked") // lambdas are not smart enough (for now) to type this properly
  public static <T> List<TwoTuple<T,T>> getEdges(TreeNode<T> root) {
    List<TwoTuple<T,T>> edges = new ArrayList<>();
    root.mapStructure((current, mappedChildren) -> {
      mappedChildren.forEach(child -> edges.add(new TwoTuple<T,T>(current, (T)child)));
      return current;
    });
    return edges;
  }

  /**
   * Retrieves the edges of the tree and applies the passed binary function to
   * each, returning a list of objects produced by the edges.
   *
   * @param <S> type of node contents
   * @param <T> type of edge object
   * @param root root node of the tree
   * @param relationCreator binary function converting an edge to an object
   * @return list of objects created from edges
   */
  public static <S,T> List<T> getEdgeObjects(TreeNode<S> root, BiFunction<S,S,T> relationCreator) {
    return getEdges(root).stream()
        .map(tup -> relationCreator.apply(tup.getFirst(), tup.getSecond()))
        .collect(Collectors.toList());
  }
}
