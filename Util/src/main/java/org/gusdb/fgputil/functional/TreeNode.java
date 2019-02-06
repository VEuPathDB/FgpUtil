package org.gusdb.fgputil.functional;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import org.gusdb.fgputil.FormatUtil.MultiLineToString;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Reducer;

import static org.gusdb.fgputil.functional.Functions.not;

/**
 * This class provides a common implementation of tree structure and the ability
 * to operate on the tree using functional interfaces.
 *
 * @author rdoherty
 */
public class TreeNode<T> implements MultiLineToString {

  /**
   * A typed predicate that returns true for leaf nodes
   */
  public final Predicate<TreeNode<T>> LEAF_PREDICATE = TreeNode::isLeaf;

  /**
   * A typed predicate that returns true for non-leaf nodes
   */
  public final Predicate<TreeNode<T>> NONLEAF_PREDICATE = not(TreeNode::isLeaf);

  /**
   * Returns a predicate that takes a TreeNode that returns true its contents
   * pass the the passed predicate, else false.
   *
   * @param predicate predicate to operate on node contents
   * @return predicate to operate on node
   */
  public Predicate<TreeNode<T>> createNodePredicate(final Predicate<T> predicate) {
    return obj -> predicate.test(obj.getContents());
  }

  /**
   * An interface to model the mapping of this tree structure to some other
   * arbitrary tree structure.
   *
   * @param <T> The type of object stored in this tree
   * @param <S> The type of a single 'node' each node in this tree will be mapped to
   */
  @FunctionalInterface
  public interface StructureMapper<T, S> {
    /**
     * Maps the contents of a node, and its already-mapped children, to the
     * node type of the new structure.
     *
     * @param obj the contents of an individual node
     * @param mappedChildren the already-mapped children of this node
     * @return a mapped object incorporating this node's contents and its children
     */
    S map(T obj, List<S> mappedChildren);
  }

  protected T _nodeContents;
  protected List<TreeNode<T>> _childNodes = new ArrayList<>();

  // private since determined by type of _nodeContents
  private final boolean _hasMultiLineSupport;

  /**
   * Creates a node containing the passed contents
   *
   * @param nodeContents
   */
  public TreeNode(T nodeContents) {
    _nodeContents = nodeContents;
    _hasMultiLineSupport = (nodeContents instanceof MultiLineToString);
  }

  public void setContents(T nodeContents) {
    _nodeContents = nodeContents;
  }

  public T getContents() {
    return _nodeContents;
  }

  public boolean isLeaf() {
    return _childNodes.isEmpty();
  }

  /**
   * Creates a new node containing the passed contents and appends it to
   * this node's list of children
   *
   * @param childContents contents of the new child node
   * @return this node
   */
  public TreeNode<T> addChild(T childContents) {
    _childNodes.add(new TreeNode<>(childContents));
    return this;
  }

  /**
   * Appends the passed node to the list of this node's children
   *
   * @param child child to append
   * @return this node
   */
  public TreeNode<T> addChildNode(TreeNode<T> child) {
    _childNodes.add(child);
    return this;
  }

  /**
   * Appends all the passed nodes to the list of this node's children
   *
   * @param children children to append
   * @return this node
   */
  public TreeNode<T> addAllChildNodes(List<TreeNode<T>> children) {
    return addChildNodes(children, new FunctionalInterfaces.TruePredicate<>());
  }

  /**
   * Appends all nodes that pass the given predicate to the list of this node's children
   *
   * @param potentialChildren potential children
   * @return this node
   */
  public TreeNode<T> addChildNodes(List<TreeNode<T>> potentialChildren, Predicate<TreeNode<T>> nodePred) {
    for (TreeNode<T> potentialChild : potentialChildren) {
      if (nodePred.test(potentialChild)) {
        _childNodes.add(potentialChild);
      }
    }
    return this;
  }

  public List<TreeNode<T>> getChildNodes() {
    return _childNodes;
  }

  public List<TreeNode<T>> getLeafNodes() {
    return findAll(LEAF_PREDICATE, null);
  }

  public List<TreeNode<T>> getNonLeafNodes() {
    return findAll(NONLEAF_PREDICATE, null);
  }

  /**
   * Finds first node in this tree whose contents match the passed predicate and
   * returns it.  Uses a depth-first search.
   *
   * @param pred predicate to test node contents against
   * @return found node or null if not found
   */
  public TreeNode<T> findFirst(Predicate<T> pred) {
    return findFirst(null, pred);
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
  public TreeNode<T> findFirst(Predicate<TreeNode<T>> nodePred, Predicate<T> pred) {
    if ((nodePred == null || nodePred.test(this)) &&
        (pred == null || pred.test(_nodeContents))) {
      return this;
    }
    for (TreeNode<T> node : _childNodes) {
      TreeNode<T> found = node.findFirst(nodePred, pred);
      if (found != null) return found;
    }
    return null;
  }

  /**
   * Finds all nodes in this tree whose contents match the passed predicate and
   * returns them.  Uses a depth-first search.
   *
   * @param pred predicate to test node contents against
   * @return list of found nodes
   */
  public List<TreeNode<T>> findAll(Predicate<T> pred) {
    return findAll(null, pred);
  }

  /**
   * Finds all nodes in this tree that match the passed node predicate and
   * whose contents match the generic predicate, and returns them.  Uses a
   * depth-first search.  Null can be passed as either predicate and evaluates
   * to 'true'.
   *
   * @param nodePred predicate to test nodes against
   * @param pred predicate to test node contents against
   * @return list of found nodes
   */
  public List<TreeNode<T>> findAll(Predicate<TreeNode<T>> nodePred, Predicate<T> pred) {
    List<TreeNode<T>> matches = new ArrayList<>();
    if ((nodePred == null || nodePred.test(this)) &&
        (pred == null || pred.test(_nodeContents))) {
      matches.add(this);
    }
    for (TreeNode<T> node : _childNodes) {
      matches.addAll(node.findAll(nodePred, pred));
    }
    return matches;
  }

  /**
   * Finds all nodes in this tree that match the passed node predicate and
   * whose contents match the generic predicate, and returns a list of outputs
   * generated by passing those nodes' contents to the passed mapper.  Uses a
   * depth-first search.  Null can be passed as either predicate and evaluates
   * to 'true'.  Passing null for the mapper will result in a
   * NullPointerException.
   *
   * @param nodePred predicate to test nodes against
   * @param pred predicate to test node contents against
   * @param mapper transform to use to map the found nodes' contents into other
   * data
   * @return list of function results
   */
  public <S> List<S> findAndMap(Predicate<TreeNode<T>> nodePred, Predicate<T> pred, Function<T,S> mapper) {
    List<S> matches = new ArrayList<>();
    if ((nodePred == null || nodePred.test(this)) &&
        (pred == null || pred.test(_nodeContents))) {
      matches.add(mapper.apply(this._nodeContents));
    }
    for (TreeNode<T> node : _childNodes) {
      matches.addAll(node.findAndMap(nodePred, pred, mapper));
    }
    return matches;
  }

  /**
   * Aggregates information in this tree into a single value, with behavior
   * defined by the passed Reducer.
   *
   * @param reducer reducer to use to aggregate information
   * @return result
   */
  public <S> S reduce(Reducer<T,S> reducer) {
    return reduce(null, reducer, null);
  }

  /**
   * Aggregates information in the nodes that pass the predicate into a single
   * value, with behavior defined by the passed Reducer.
   *
   * @param nodePred predicate to filter nodes that will contribute to the reduction
   * @param reducer reducer to use to aggregate information
   * @param initialValue initial value passed to the first call to the reducer's reduce method
   * @return result, or initialValue if no nodes match the predicate
   */
  public <S> S reduce(Predicate<TreeNode<T>> nodePred, Reducer<T,S> reducer, S initialValue) {
    if (nodePred == null || nodePred.test(this)) {
      initialValue = reducer.reduce(initialValue, _nodeContents);
    }
    for (TreeNode<T> node : _childNodes) {
      initialValue = node.reduce(nodePred, reducer, initialValue);
    }
    return initialValue;
  }

  /**
   * Enables a mapping of a TreeNode tree structure to an arbitrary tree
   * structure of a different type.
   *
   * @param mapper maps an individual node and its children to the new type
   * @return a mapped object
   */
  public <S> S mapStructure(StructureMapper<T,S> mapper) {
    // first create list of mapped child objects
    List<S> mappedChildren = new ArrayList<>();
    for (TreeNode<T> child : _childNodes) {
      mappedChildren.add(child.mapStructure(mapper));
    }
    // pass this object plus converted children to mapper
    return mapper.map(_nodeContents, mappedChildren);
  }

  /**
   * Returns a clone of this tree.  This is a "deep" clone in the sense that all
   * child nodes are also replicated; however the contents of the nodes are not
   * cloned.  The objects referred to in the clone are the same as those
   * referred to in the original.  NOTE: this is simply a special case of
   * <code>mapStructure()</code>.
   *
   * @return clone of this tree
   */
  @Override
  public TreeNode<T> clone() {
    return mapStructure((obj, mappedChildren) -> {
      TreeNode<T> copy = new TreeNode<T>(obj);
      copy._childNodes = mappedChildren;
      return copy;
    });
  }

  /**
   * Removes any subtrees that pass the passed predicate
   *
   * @param pred predicate to test node contents against
   * @return number of subtrees removed
   */
  public int removeAll(Predicate<T> pred) {
    int numRemoved = 0;
    for (int i = 0; i < _childNodes.size(); i++) {
      if (pred.test(_childNodes.get(i)._nodeContents)) {
        _childNodes.remove(i);
        numRemoved++;
        i--; // reuse the current index, now pointing to the next node
      }
      else {
        numRemoved += _childNodes.get(i).removeAll(pred);
      }
    }
    return numRemoved;
  }

  /**
   * Removes any subtrees that pass the passed predicate
   *
   * @param pred predicate to test node contents against
   * @return number of subtrees removed
   */
  public int removeAllNodes(Predicate<TreeNode<T>> pred) {
    int numRemoved = 0;
    for (int i = 0; i < _childNodes.size(); i++) {
      if (pred.test(_childNodes.get(i))) {
        _childNodes.remove(i);
        numRemoved++;
        i--; // reuse the current index, now pointing to the next node
      }
      else {
        numRemoved += _childNodes.get(i).removeAllNodes(pred);
      }
    }
    return numRemoved;
  }

  /**
   * Replaces each node's contents with the result of the passed function
   *
   * @param function function to apply to each node
   */
  public void apply(Function<T, T> function) {
    apply(null, null, function);
  }

  /**
   * Replaces each node's contents with the result of the passed function, but
   * only if that node's contents pass the passed predicate.
   *
   * @param pred     predicate to filter nodes to which the function should be
   *                 applied
   * @param function function to apply
   */
  public void apply(Predicate<T> pred, Function<T, T> function) {
    apply(null, pred, function);
  }

  /**
   * Replaces each node's contents with the result of the passed function, but
   * only if that node passes the passed predicate.
   *
   * @param function function to apply
   * @param pred     predicate to filter nodes to which the function should be
   *                 applied
   */
  public void apply(Function<T, T> function, Predicate<TreeNode<T>> pred) {
    apply(pred, null, function);
  }

  /**
   * Replaces each node's contents with the result of the passed function, but
   * only if that node passes both the passed predicates.
   *
   * @param nodePred predicate to filter nodes to which the function should be
   *                 applied (tests node)
   * @param pred     predicate to filter nodes to which the function should be
   *                 applied (tests node contents)
   * @param function function to apply
   */
  public void apply(Predicate<TreeNode<T>> nodePred, Predicate<T> pred, Function<T, T> function) {
    if ((nodePred == null || nodePred.test(this)) &&
        (pred == null || pred.test(_nodeContents))) {
      _nodeContents = function.apply(_nodeContents);
    }
    for (TreeNode<T> node : _childNodes) {
      node.apply(nodePred, pred, function);
    }
  }

  public List<List<TreeNode<T>>> findCircularPaths() {
    // TODO: implement this
    throw new UnsupportedOperationException();
  }

  /**
   * @return The total count of nodes in this tree.
   */
  public int size() {
    return 1 + _childNodes.stream().mapToInt(TreeNode::size).sum();
  }

  /**
   * Flatten the tree into a Collection of it's contents.
   *
   * @return Collection of the contents of this tree node in order of depth.
   */
  public Collection<T> flatten() {
    final int len = size();
    final List<T> out = new ArrayList<>(len);
    final Queue<TreeNode<T>> next = new LinkedList<>();

    next.offer(this);

    int pos = 0;

    while(!next.isEmpty()) {
      final TreeNode<T> cur = next.poll();
      out.set(pos++, cur._nodeContents);
      cur._childNodes.forEach(next::offer);
    }

    return out;
  }

  /**
   * Returns a string representation of this node and its subtree.
   */
  @Override
  public String toString() {
    return isLeaf() ? "Leaf { " + _nodeContents + " }" : toMultiLineString("");
  }

  @Override
  public String toMultiLineString(String ind) {
    String NL = System.lineSeparator();

    String nodeString = (!_hasMultiLineSupport ? _nodeContents.toString() :
      ((MultiLineToString)_nodeContents).toMultiLineString(ind + "  "));
    StringBuilder str = new StringBuilder()
        .append(ind).append("TreeNode {").append(NL)
        .append(ind).append("  ").append(nodeString).append(NL)
        .append(ind).append("  Leaves:").append(NL);
    for (TreeNode<T> node : _childNodes) {
      if (node.isLeaf()) {
        str.append(node.toMultiLineString(ind + "    ")).append(NL);
      }
    }
    str.append(ind).append("  Children {").append(NL);
    for (TreeNode<T> child : _childNodes) {
      if (!child.isLeaf()) {
        str.append(child.toMultiLineString(ind + "    "));
      }
    }
    str.append(ind).append("  }").append(NL).append(ind).append("}").append(NL);
    return str.toString();
  }
}
