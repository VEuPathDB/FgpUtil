package org.gusdb.fgputil.interview;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.junit.Test;

import org.junit.Assert;

/**
 * Follow-up interview questions just in case we need them after the tech interview
 * 
 * @author rdoherty
 */
public class InterviewFollowUp {

  //=======================================================
  // Problem #1: Reverse the nodes of a tree
  //=======================================================

  /**
   * Class representing a node of a homogeneous binary tree
   */
  public static class Node<T> {

    public T content;
    public Node<T> leftChild;
    public Node<T> rightChild;

    public Node(T content, Node<T> leftChild, Node<T> rightChild) {
      this.content = content; this.leftChild = leftChild; this.rightChild = rightChild;
    }
  }

  /**
   * Takes a node of a tree and returns a clone of tree where the nodes are
   * reversed (i.e. left and right children are swapped).
   * @param root
   * @return
   */
  public <T> Node<T> reverseTree(Node<T> root) {
    // TODO implement
    return root;
  }

  //=======================================================
  // Problem #2: Reverse the lines of a file
  //=======================================================

  /**
   * Writes the lines of the (text) file referred to by inFile to outFile,
   * reversing the lines (delimited by '\n').
   * 
   * @param inFile locaion of file to read
   * @param outFile location to write reversed lines
   */
  public void reverseLinesOfFile(Path inFile, Path outFile) {
    // TODO implement
  }

  //=======================================================
  // Problem #3a: Tree reduce: show, have them explain
  //=======================================================

  /**
   * Class representing a node of a generalized homogeneous tree (DAG)
   */
  public static class Node2<T> {

    public T content;
    public List<Node2<T>> children;

    public Node2(T content, List<Node2<T>> children) {
      this.content = content; this.children = children;
    }
  }

  /**
   * Reduces the tree with the passed root to a new value using the passed reducer.  This
   * functions traverses the tree bottom up, reducing children first and passing the
   * results as a list to their parent.  Finally the root of the tree is reduced using
   * its value and the reductions of its direct children.
   * 
   * @param root tree node 
   * @param reducer function to reduce each node and its children to a new value
   * @return reduction of the tree
   */
  public static <S,T> S reduceTree(Node2<T> root, BiFunction<Node2<T>,List<S>,S> reducer) {
    return reducer.apply(root, root.children.stream()
        .map(node -> reduceTree(node, reducer))
        .collect(Collectors.toList()));
  }

  //=======================================================
  // Problem #3b: Tree reduce: show, have them explain
  //=======================================================

  private static final Node2<Integer> integerTree =
    node(4, children(
      node(2, children(
        node(1),
        node(3)
      )),
      node(6, children(
        node(5),
        node(7)
      ))
    ));

  @Test
  public void reduceTreeSample() {
    JSONObject result = reduceTree(integerTree, (currentNode, convertedChildren) ->
      new JSONObject()
        .put("value", currentNode.content)
        .put("children", convertedChildren));
    System.out.println(result.toString(2));
  }

  //=======================================================
  // Show above as an example, then have them implement one or more of the following functions
  //=======================================================

  public static int countNodesNoStream(Node2<?> root) {
    return reduceTree(root, (currentNode, convertedChildren) -> {
      int numNodes = 1;
      for (int childCount : convertedChildren) {
        numNodes += childCount;
      }
      return numNodes;
    });
  }

  public static int countNodesWithStream(Node2<?> root) {
    return reduceTree(root, (currentNode, convertedChildren) ->
      convertedChildren.stream().reduce(0, (a, b) -> a + b) + 1);
  }

  public static <T> int countNodes(Node2<T> root, Predicate<Node2<T>> condition) {
    return reduceTree(root, (currentNode, convertedChildren) ->
      convertedChildren.stream().reduce(0, Integer::sum) +
      (condition.test(currentNode) ? 1 : 0));
  }

  //=======================================================
  // Unit tests for above
  //=======================================================

  @Test
  public void testCountNodes() {
    // count all nodes
    Assert.assertEquals(7, countNodesNoStream(integerTree));
    // count all nodes
    Assert.assertEquals(7, countNodesWithStream(integerTree));
    // count all nodes
    Assert.assertEquals(7, countNodes(integerTree, node -> true));
    // count nodes where content > 4
    Assert.assertEquals(3, countNodes(integerTree, node -> node.content > 4));
    // count nodes where content is odd
    Assert.assertEquals(4, countNodes(integerTree, node -> node.content % 2 == 1));
    // count leaf nodes
    Assert.assertEquals(4, countNodes(integerTree, node -> node.children.isEmpty()));
  }

  //=======================================================
  // Utilities for conveniently creating a tree in-line
  //=======================================================

  private static <T> Node2<T> node(T i) {
    return new Node2<T>(i, Collections.EMPTY_LIST);
  }

  private static <T> Node2<T> node(T i, List<Node2<T>> children) {
    return new Node2<>(i, children);
  }

  @SafeVarargs
  private static <T> List<Node2<T>> children(Node2<T>... nodes) {
    return Arrays.asList(nodes);
  }

}
