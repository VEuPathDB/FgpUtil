package org.gusdb.fgputil.workflow;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;

public class DependencyResolver<T extends DependencyElement<T>> {

  private static Logger LOG = Logger.getLogger(DependencyResolver.class);

  private class Node {

    private final T _element;

    // map of nodes this node depends on
    private Map<String, Node> _dependents = new HashMap<>();
    private Map<String, Node> _savedDependents = new HashMap<>();

    // map of nodes that depend on this node
    private Map<String, Node> _dependeds = new HashMap<>();
    private Map<String, Node> _savedDependeds = new HashMap<>();

    public Node(T element) {
      _element = element;
    }

    public String getKey() {
      return _element.getKey();
    }

    public T getElement() {
      return _element;
    }

    public Set<T> getDependedElements() {
      return _element.getDependedElements();
    }

    public void resolveDependedNodes(Map<String, Node> nodeMap) {
      for (T element : getDependedElements()) {
        _dependeds.put(element.getKey(), nodeMap.get(element.getKey()));
      }
    }

    public Map<String, Node> getDependedNodes() {
      return _dependeds;
    }

    public void addDependent(Node node) {
      _dependents.put(node.getKey(), node);
    }

    public Map<String, Node> getDependents() {
      return _dependents;
    }

    public void applyDependents(List<Node> orderedNodes) {
      // still only have references to nodes directly dependent on this node;
      //   need to fetch ALL dependents, direct and indirect
      Set<Node> allDependents = new HashSet<>();
      LOG.debug("Loading all dependents for node " + getKey());
      loadDependents(allDependents, _dependents.values());
      LOG.debug("Done. All dependents of " + getKey() + ": " +
          FormatUtil.arrayToString(allDependents.toArray()));
      List<T> elementList = new ArrayList<T>();
      for (Node node : orderedNodes) {
        if (allDependents.contains(node)) {
          elementList.add(node._element);
        }
      }
      _element.setDependentElements(elementList);
    }

    private void loadDependents(Set<Node> allDependents, Collection<Node> newDependents) {
      for (Node newDep : newDependents) {
        LOG.debug("Adding " + newDep + " and (hopefully) its dependents: " +
            FormatUtil.arrayToString(newDep.getDependents().values().toArray()));
        allDependents.add(newDep);
        loadDependents(allDependents, newDep.getDependents().values());
      }
    }

    public void removeDependent(Node node) {
      node._dependeds.remove(getKey());
      _dependents.remove(node.getKey());
    }

    @Override
    public String toString() {
      return getKey();
    }

    public void saveDependencies() {
      _savedDependents.putAll(_dependents);
      _savedDependeds.putAll(_dependeds);
    }

    public void resetDependencies() {
      _dependents = new HashMap<>(_savedDependents);
      _dependeds = new HashMap<>(_savedDependeds);
    }
  }

  private Map<String, T> _elementMap = new HashMap<>();

  public DependencyResolver<T> addElements(@SuppressWarnings("unchecked") T... elements) {
    for (T element : elements) {
      if (_elementMap.containsKey(element.getKey())) {
        throw new RuntimeException("Cannot add multiple elements " +
            "with the same key ['" + element.getKey() + "'].");
      }
      _elementMap.put(element.getKey(), element);
    }
    return this;
  }

  public List<T> resolveDependencyOrder() {

    // build node map
    Map<String, Node> nodeMap = new HashMap<>();
    for (T element : _elementMap.values()) {
      nodeMap.put(element.getKey(), new Node(element));
    }

    // assign dependent elements to nodes
    for (Node node : nodeMap.values()) {
      for (T dependedElement : node.getDependedElements()) {
        // node depends on dependedElement; tell dependedElement node is dependent
        nodeMap.get(dependedElement.getKey()).addDependent(node);
      }
    }

    // resolve dependencies to reference Nodes instead of elements
    for (Node node : nodeMap.values()) {
      node.resolveDependedNodes(nodeMap);
      node.saveDependencies();
    }

    // collect independent nodes as roots
    List<Node> rootNodes = new ArrayList<>();
    for (Node node : nodeMap.values()) {
      if (node.getDependedElements().isEmpty()) {
        // node is a root node (does not depend on any other nodes)
        rootNodes.add(node);
      }
    }

    // sort the nodes into a dependency path
    List<Node> nodeOrdering = topologicalSort(rootNodes, nodeMap.size());

    // set dependents on elements
    for (Node node : nodeMap.values()) {
      node.resetDependencies();
    }
    for (Node node : nodeMap.values()) {
      node.applyDependents(nodeOrdering);
    }
    
    // convert sorted list back to elements and return
    List<T> result = new ArrayList<>();
    for (Node node : nodeOrdering) {
      result.add(node.getElement());
    }
    return result;
  }

  /**
   * Implementation of algorithm found here:
   *   http://en.wikipedia.org/wiki/Topological_sorting
   * 
   * L ← Empty list that will contain the sorted elements
   * S ← Set of all nodes with no incoming edges
   * while S is non-empty do
   *   remove a node n from S
   *   add n to tail of L
   *   for each node m with an edge e from n to m do
   *     remove edge e from the graph
   *     if m has no other incoming edges then
   *       insert m into S
   * if graph has edges then
   *   return error (graph has at least one cycle)
   * else 
   *   return L (a topologically sorted order)
   * 
   * @param rootNodeList list of root nodes (independent nodes)
   * @param totalNodes number of nodes expected in final sorted list
   * @return topologically sorted node list
   * @throws RuntimeException if cycle detected
   */
  private List<Node> topologicalSort(List<Node> rootNodeList, int totalNodes) {
    List<Node> orderedNodes = new LinkedList<>();           // L
    List<Node> rootNodes = new LinkedList<>(rootNodeList);  // S
    while (!rootNodes.isEmpty()) {
      LOG.debug("Root nodes: " + FormatUtil.arrayToString(rootNodes.toArray()));
      Node n = rootNodes.remove(0);
      orderedNodes.add(n);
      LOG.debug("Added " + n + " to orderedNodes");
      for (Node m : new ArrayList<Node>(n.getDependents().values())) {
        n.removeDependent(m);
        LOG.debug("Made " + m + " no longer depend on " + n + ", " + m + " still depends on " +
            FormatUtil.arrayToString(m.getDependedNodes().values().toArray()));
        if (m.getDependedNodes().isEmpty()) {
          // m has no other dependencies
          LOG.debug("Node " + m + " has no other depended nodes, adding to rootNodes");
          rootNodes.add(m);
        }
      }
    }
    LOG.debug("Ordered nodes: " + FormatUtil.arrayToString(orderedNodes.toArray()));
    LOG.debug("Root nodes remaining: " + FormatUtil.arrayToString(rootNodes.toArray()));
    if (orderedNodes.size() == totalNodes) {
      // all nodes were placed in ordered list
      return orderedNodes;
    }
    throw new RuntimeException("Cyclic dependency detected.");
  }

  public List<String> convertToKeyList(List<T> list) {
    List<String> strList = new ArrayList<>();
    for (T element : list) {
      strList.add(element.getKey());
    }
    return strList;
  }
}
