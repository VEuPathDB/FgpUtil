package org.gusdb.fgputil.functional;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

// TODO: move utility methods from TreeNode to here and convert to pure functions
public class TreeUtil {

  private TreeUtil(){}

  /**
   * Sample Usage:
   *
   * private static class Entity { public String getName() { return null; }}
   * TreeNode<Entity> entityTree;
   * String targetEntityName;
   * List<String> filteredEntityNames;
   *
   * TreeNode<Entity> trimmedRoot = trimToActiveAndPivotNodes(entityTree,
   *   e -> targetEntityName.equals(e.getName()) || filteredEntityNames.contains(e.getName()));
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
}
