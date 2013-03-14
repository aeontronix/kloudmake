/*
 * Copyright (c) 2013 KloudTek Ltd
 */

package com.kloudtek.systyrant.resource;

import com.kloudtek.systyrant.exception.InvalidDependencyException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ResourceSorter {
    static class Node {
        final Resource resource;
        final HashSet<Dependency> inDependencies;
        final HashSet<Dependency> outDependencies;
        boolean visited;

        public Node(Resource resource) {
            this.resource = resource;
            inDependencies = new HashSet<>();
            outDependencies = new HashSet<>();
        }

        public Node addDependency(Node node) {
            Dependency e = new Dependency(this, node);
            outDependencies.add(e);
            node.inDependencies.add(e);
            return this;
        }
    }

    static class Dependency {
        public final Node from;
        public final Node to;

        public Dependency(Node from, Node to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Dependency) {
                Dependency e = (Dependency) obj;
                return e.from == from && e.to == to;
            } else {
                return false;
            }
        }
    }

    public static void sort(List<Resource> resources) throws InvalidDependencyException {
        if (resources.isEmpty()) {
            return;
        }
        //  L ← Empty list that will contain the sorted nodes
        List<Node> sortedList = new ArrayList<>();
        //  S ← Set of all nodes with no outgoing edges
        List<Node> nodes = e2n(resources);
        HashSet<Node> edgeNodes = findEdgeNodes(nodes);
        if (edgeNodes.isEmpty()) {
            throw new InvalidDependencyException("Circular dependency between all resources");
        }
        //  for each node n in S do
        for (Node node : edgeNodes) {
            ArrayList<Node> stack = new ArrayList<>();
            // visit(n)
            visit(node, sortedList, stack);
        }
        resources.clear();
        resources.addAll(n2e(sortedList));
    }

    private static void visit(Node n, List<Node> sortedList, ArrayList<Node> stack) throws InvalidDependencyException {
        // if n has not been visited yet then
        if (stack.contains(n)) {
            StringBuilder err = new StringBuilder("Circular dependency: ");
            boolean first = true;
            for (Node node : stack.subList(stack.indexOf(n), stack.size())) {
                if (first) {
                    first = false;
                } else {
                    err.append(" -> ");
                }
                err.append(node.resource.toString());
            }
            err.append(" -> ").append(n.resource);
            throw new InvalidDependencyException(err.toString());
        }
        if (!n.visited) {
            // mark n as visited
            n.visited = true;
            stack.add(n);
            // for each node m with an edge from m to n do
            for (Dependency dep : n.outDependencies) {
                // visit(m)
                visit(dep.to, sortedList, new ArrayList<>(stack));
            }
            // add n to L
            sortedList.add(n);
        }
    }

    private static List<Node> e2n(List<Resource> resources) {
        HashMap<Resource, Node> map = new HashMap<>();
        ArrayList<Node> list = new ArrayList<>();
        for (Resource resource : resources) {
            map.put(resource, new Node(resource));
            list.add(map.get(resource));
        }
        for (Resource el : resources) {
            for (Resource dep : el.getDependencies()) {
                map.get(el).addDependency(map.get(dep));
            }
        }
        return list;
    }

    private static List<Resource> n2e(List<Node> nodeList) {
        ArrayList<Resource> resourceList = new ArrayList<>(nodeList.size());
        for (Node node : nodeList) {
            resourceList.add(resourceList.size(), node.resource);
        }
        return resourceList;
    }

    private static HashSet<Node> findEdgeNodes(List<Node> allNodes) {
        HashSet<Node> list = new HashSet<>();
        for (Node n : allNodes) {
            if (n.inDependencies.size() == 0) {
                list.add(n);
            }
        }
        return list;
    }
}