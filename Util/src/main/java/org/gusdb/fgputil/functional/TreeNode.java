package org.gusdb.fgputil.functional;

import java.util.ArrayList;
import java.util.List;

import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Predicate;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Reducer;

/**
 * This class provides a common implementation of tree structure, and it is used
 * by the category for attributes and tree params.
 * 
 * @author rdoherty
 */
public class TreeNode<T> {

  public interface MultiLineToString {
    public String toMultiLineString(String indentation);
  }

  public final Predicate<TreeNode<T>> ANY_NODE_PREDICATE = new Predicate<TreeNode<T>>() {
    @Override public boolean test(TreeNode<T> obj) { return true; } };
    
  public final Predicate<TreeNode<T>> LEAF_PREDICATE = new Predicate<TreeNode<T>>() {
    @Override public boolean test(TreeNode<T> obj) { return obj.isLeaf(); } };

  public final Predicate<TreeNode<T>> NONLEAF_PREDICATE = new Predicate<TreeNode<T>>() {
    @Override public boolean test(TreeNode<T> obj) { return !obj.isLeaf(); } };
      
  private T _nodeContents;
  private final boolean _hasMultiLineSupport;
  private List<TreeNode<T>> _childNodes = new ArrayList<>();

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

  public TreeNode<T> addChild(T childContents) {
    _childNodes.add(new TreeNode<T>(childContents));
    return this;
  }

  public TreeNode<T> addChildNode(TreeNode<T> child) {
    _childNodes.add(child);
    return this;
  }

  public List<TreeNode<T>> getChildNodes() {
    return _childNodes;
  }
  
  /**
   * Finds first node in this tree with the passed name and returns it
   * 
   * @param name name of desired node
   * @return found node or null if not found
   */
  public TreeNode<T> findFirst(Predicate<T> pred) {
    return findFirst(null, pred);
  }

  public TreeNode<T> findFirst(Predicate<TreeNode<?>> nodePred, Predicate<T> pred) {
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

  public List<TreeNode<T>> findAll(Predicate<T> pred) {
    return findAll(null, pred);
  }

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

  public <S> S reduce(Reducer<T,S> reducer) {
    S current = reducer.reduce(_nodeContents);
    for (TreeNode<T> node : _childNodes) {
      current = reducer.reduce(node._nodeContents, current);
    }
    return current;
  }

  /**
   * Removes any subtrees with the passed name.
   * 
   * @param name name of the subtree to be removed
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
   * Replaces each node's contents with the result of the passed function
   * 
   * @param function
   */
  public void replace(Function<T, T> function) {
    _nodeContents = function.apply(_nodeContents);
    for (TreeNode<T> node : _childNodes) {
      node.replace(function);
    }
  }

  /**
   * Replaces each node's contents with the result of the passed function
   * 
   * @param function
   */
  public void apply(Predicate<T> pred, Function<T, T> function) {
    apply(null, pred, function);
  }

  /**
   * Replaces each node's contents with the result of the passed function
   * 
   * @param function
   */
  public void apply(Function<T, T> function, Predicate<TreeNode<T>> pred) {
    apply(pred, null, function);
  }

  /**
   * Replaces each node's contents with the result of the passed function
   * 
   * @param function
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

  @Override
  public String toString() {
    if (isLeaf()) {
      return leafToString();
    }
    return toString("");
  }

  public String toString(String indentation) {
    String IND = indentation;
    String NL = System.getProperty("line.separator");
    String nodeString = (!_hasMultiLineSupport ? _nodeContents.toString() :
      ((MultiLineToString)_nodeContents).toMultiLineString(IND + "  "));
    StringBuilder str = new StringBuilder()
        .append(IND).append("TreeNode {").append(NL)
        .append(IND).append("  ").append(nodeString).append(NL)
        .append(IND).append("  Leaves:").append(NL);
    for (TreeNode<T> node : _childNodes) {
      if (node.isLeaf()) {
        str.append(node.toString(IND + "    ")).append(NL);
      }
    }
    str.append(IND).append("  Children {").append(NL);
    for (TreeNode<T> child : _childNodes) {
      if (!child.isLeaf()) {
        str.append(child.toString(IND + "    "));
      }
    }
    str.append(IND).append("  }").append(NL).append(IND).append("}").append(NL);
    return str.toString();
  }

  private String leafToString() {
    return new StringBuilder().append("Leaf { ").append(_nodeContents).append(" }").toString();
  }
}
