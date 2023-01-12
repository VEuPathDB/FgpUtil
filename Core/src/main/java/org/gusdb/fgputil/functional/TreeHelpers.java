package org.gusdb.fgputil.functional;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.gusdb.fgputil.Tuples.TwoTuple;

/**
 * Contains static functions that interrogate or operate on the TreeNode implementation of trees.
 *
 * @author rdoherty
 */
public class TreeHelpers {

  // static class
  private TreeHelpers() {}

  /**
   * Returns a predicate that takes a TreeNode that returns true iff
   * its contents pass the the passed predicate.
   *
   * @param predicate predicate to operate on node contents
   * @return predicate to operate on node
   */
  public static <T> Predicate<TreeNode<T>> toNodePredicate(final Predicate<T> predicate) {
    return node -> predicate.test(node.getContents());
  }

  /**
   * An interface to model the mapping of this tree structure to some other
   * arbitrary tree structure.
   *
   * @param <T> The type of object stored in this tree
   * @param <S> The type of a single 'node' each node in this tree will be mapped to
   */
  @FunctionalInterface
  public interface StructureMapper<T, S> extends BiFunction<T, List<S>, S> {
    /**
     * Maps the contents of a node, and its already-mapped children, to the
     * node type of the new structure.
     *
     * @param obj the contents of an individual node
     * @param mappedChildren the already-mapped children of this node
     * @return a mapped object incorporating this node's contents and its children
     */
    @Override
    S apply(T obj, List<S> mappedChildren);
  }

  /**
   * Enables a mapping of a TreeNode tree structure to an arbitrary tree
   * structure of a different type.
   *
   * @param mapper maps an individual node and its children to the new type
   * @return a mapped object
   */
  public static <T,S> S mapStructure(TreeNode<T> root, StructureMapper<T,S> mapper) {
    // first create list of mapped child objects
    List<S> mappedChildren = new ArrayList<>();
    for (TreeNode<T> child : root.getChildNodes()) {
      mappedChildren.add(mapStructure(child, mapper));
    }
    // pass this object plus converted children to mapper
    return mapper.apply(root.getContents(), mappedChildren);
  }

  /**
   * Finds first node in this tree that matches the passed node predicate and
   * whose contents match the generic predicate, and returns it.  Uses a
   * depth-first search.  Null can be passed as either predicate and evaluates
   * to 'true'.
   *
   * @param nodePred predicate to test nodes against
   * @param pred predicate to test node contents against
   * @return found node or null if not found
   */
  public static <T> Optional<TreeNode<T>> findFirst(TreeNode<T> root, Predicate<TreeNode<T>> predicate) {
    if (predicate.test(root)) {
      return Optional.of(root);
    }
    for (TreeNode<T> child : root.getChildNodes()) {
      Optional<TreeNode<T>> found = findFirst(child, predicate);
      if (found.isPresent()) {
        return found;
      }
    }
    return Optional.empty();
  }

  /**
   * Finds all nodes in this tree that match the passed predicate and returns
   * them.  Uses a depth-first search (determining list order).
   *
   * @param predicate predicate to test nodes against
   * @return list of found nodes
   */
  public static <T> List<TreeNode<T>> findAll(TreeNode<T> root, Predicate<TreeNode<T>> predicate) {
    List<TreeNode<T>> matches = new ArrayList<>();
    if (predicate.test(root)) {
      matches.add(root);
    }
    for (TreeNode<T> child : root.getChildNodes()) {
      matches.addAll(findAll(child, predicate));
    }
    return matches;
  }

  /**
   * Removes any subtrees that pass the passed predicate.  This is a top-down
   * operation, so e.g. an TreeNode::isLeaf predicate will not result in an
   * empty tree.  The root node is NOT tested since that API would be ambiguous.
   *
   * @param pred predicate to test node against
   * @return number of subtrees removed
   */
  public static <T> int removeSubtrees(TreeNode<T> root, Predicate<TreeNode<T>> pred) {
    int numRemoved = 0;
    List<TreeNode<T>> children = root.getChildNodes(); // not a copy
    for (int i = 0; i < children.size(); i++) {
      if (pred.test(children.get(i))) {
        children.remove(i);
        numRemoved++;
        i--; // reuse the current index, now pointing to the next node
      }
      else {
        // keep this node but search children for nodes to remove
        numRemoved += removeSubtrees(children.get(i), pred);
      }
    }
    return numRemoved;
  }

  /**
   * Replaces each node's contents with the result of the passed function
   *
   * @param root root of the tree
   * @param function function to apply to each node
   */
  public <T> void apply(TreeNode<T> root, Function<T, T> function) {
    apply(root, function, Functions.alwaysTrue());
  }

  /**
   * Replaces each node's contents with the result of the passed function, but
   * only if that node passes the passed predicate.
   *
   * @param root root of the tree
   * @param function function to apply
   * @param predicate predicate to filter nodes to which the function should be applied
   */
  public static <T> void apply(TreeNode<T> root, Function<T, T> function, Predicate<TreeNode<T>> predicate) {
    if (predicate.test(root)) {
      root.setContents(function.apply(root.getContents()));
    }
    for (TreeNode<T> child : root.getChildNodes()) {
      apply(child, function, predicate);
    }
  }

  /**
   * Finds any circular paths in this "tree" i.e. assesses the validity of the tree
   * structure.  If any circular paths are found, the recursive helper methods (and
   * any independently written ones) run against this tree will never end and result
   * in stack exhaustion.  Thus, if a tree is generated from unknown input, it may
   * be a good idea to validate the tree using this method before operating on it.
   * A non-empty list indicates an invalid tree.  The paths are included so callers
   * can display the circular paths in an error message.
   *
   * @param <T> type of the tree
   * @param root root node
   * @return list of circular paths
   */
  public static <T> List<List<TreeNode<T>>> findCircularPaths(TreeNode<T> root) {
    // TODO: implement this
    throw new UnsupportedOperationException();
  }

  /**
   * Flatten the tree into a Collection of its contents in breadth-first order.
   *
   * @param <T> type of the tree
   * @param root root of the tree
   * @return list of the contents of this tree node in order of depth.
   */
  public static <T> List<T> flatten(TreeNode<T> root) {

    final List<T> out = new ArrayList<>(root.size());
    final Queue<TreeNode<T>> next = new LinkedList<>();

    next.offer(root);

    while(!next.isEmpty()) {
      final TreeNode<T> cur = next.poll();
      out.add(cur._nodeContents);
      cur._childNodes.forEach(next::offer);
    }

    return out;
  }

  /**
   * Given a tree, returns a copy, trimmed to only nodes of interest (active
   * nodes) and their shared ancestors.  Inactive nodes and parents of only
   * one active node are removed.
   * 
   * Sample usage that may support a trimmed tree for SQL joins:
   *
   * private static class Entity { public String getName() { return null; }}
   * TreeNode&lt;Entity> entityTree;
   * String targetEntityName;
   * List&lt;String> filteredEntityNames;
   *
   * TreeNode&lt;Entity> trimmedRoot = trimToActiveAndPivotNodes(entityTree,
   *   e -> targetEntityName.equals(e.getName()) || filteredEntityNames.contains(e.getName()));
   */
  public static <T> TreeNode<T> trimToActiveAndPivotNodes(TreeNode<T> root, Predicate<T> isActive) {
    return mapStructure(root, (nodeContents, mappedChildren) -> {
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
    mapStructure(root, (current, mappedChildren) -> {
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
