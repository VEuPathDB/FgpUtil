package org.gusdb.fgputil.functional;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.functional.Functions.alwaysTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.gusdb.fgputil.FormatUtil.MultiLineToString;

/**
 * This class provides a common implementation of tree structure and the ability
 * to operate on the tree using functional interfaces.
 *
 * @author rdoherty
 */
public class TreeNode<T> implements MultiLineToString {

  /**
   * Contents object of this node
   */
  protected T _nodeContents;

  /**
   * Children of this node
   */
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

  public boolean hasChildren() {
    return !_childNodes.isEmpty();
  }

  /**
   * @return The total count of nodes in this tree
   */
  public int size() {
    return 1 + _childNodes.stream().mapToInt(TreeNode::size).sum();
  }

  /**
   * Creates a new node containing the passed contents and appends it to
   * this node's list of children
   *
   * @param childContents contents of the new child node
   * @return this node
   */
  public TreeNode<T> addChild(T childContents) {
    return addChildNode(new TreeNode<>(childContents));
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
    return addChildNodes(children, alwaysTrue());
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
    return TreeHelpers.mapStructure(this, (obj, mappedChildren) -> {
      TreeNode<T> copy = new TreeNode<T>(obj);
      copy._childNodes = mappedChildren;
      return copy;
    });
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
    String nodeString = (!_hasMultiLineSupport ? _nodeContents.toString() :
      ((MultiLineToString)_nodeContents).toMultiLineString(ind + "  "));
    StringBuilder str = new StringBuilder()
        .append(ind).append("TreeNode {").append(NL)
        .append(ind).append("  Contents: ").append(nodeString).append(NL);
    if (!_childNodes.isEmpty()) {
      str.append(ind).append("  Children: [").append(NL);
      for (TreeNode<T> child : _childNodes) {
        str.append(child.toMultiLineString(ind + "    "));
      }
      str.append(ind).append("  ]").append(NL);
    }
    str.append(ind).append("}").append(NL);
    return str.toString();
  }
}
