/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Utility class that provides topological sort.
 *
 * <p>
 * Note this class is non-public to allow for API transition.
 */
@Beta
public final class TopologicalSort {

    /**
     * It isn't desirable to create instance of this class.
     */
    private TopologicalSort() {
    }

    /**
     * Topological sort of dependent nodes in acyclic graphs.
     *
     * @param nodes graph nodes
     * @return Sorted {@link List} of {@link Node}s. Order: Nodes with no dependencies starting.
     * @throws IllegalStateException when cycle is present in the graph
     */
    public static List<Node> sort(final Set<Node> nodes) {
        List<Node> sortedNodes = new ArrayList<>(nodes.size());

        Set<Node> dependentNodes = getDependentNodes(nodes);

        while (!dependentNodes.isEmpty()) {
            Node node = dependentNodes.iterator().next();
            dependentNodes.remove(node);

            sortedNodes.add(node);

            for (Edge edge : node.getInEdges()) {
                Node referent = edge.getFrom();
                referent.getOutEdges().remove(edge);

                if (referent.getOutEdges().isEmpty()) {
                    dependentNodes.add(referent);
                }
            }
        }

        detectCycles(nodes);

        return sortedNodes;
    }

    private static Set<Node> getDependentNodes(final Set<Node> nodes) {
        Set<Node> dependentNodes = new HashSet<>();
        for (Node node : nodes) {
            if (node.getOutEdges().isEmpty()) {
                dependentNodes.add(node);
            }
        }
        return dependentNodes;
    }

    private static void detectCycles(final Set<Node> nodes) {
        // Detect cycles
        boolean cycle = false;
        Node cycledNode = null;

        for (Node node : nodes) {
            if (!node.getOutEdges().isEmpty()) {
                cycle = true;
                cycledNode = node;
                break;
            }
        }
        checkState(!cycle, "Cycle detected in graph around node: " + cycledNode);
    }

    /**
     * Interface for nodes in graph that can be sorted topologically.
     */
    @Beta
    public interface Node {
        Set<Edge> getInEdges();

        Set<Edge> getOutEdges();
    }

    /**
     * Interface for edges in graph that can be sorted topologically.
     */
    @Beta
    public interface Edge {
        Node getFrom();

        Node getTo();
    }

    /**
     * Basic Node implementation.
     */
    @Beta
    public static class NodeImpl implements Node {
        private final Set<Edge> inEdges;
        private final Set<Edge> outEdges;

        public NodeImpl() {
            inEdges = new HashSet<>();
            outEdges = new HashSet<>();
        }

        @Override
        public Set<Edge> getInEdges() {
            return inEdges;
        }

        @Override
        public Set<Edge> getOutEdges() {
            return outEdges;
        }

        public void addEdge(final Node to) {
            Edge edge = new EdgeImpl(this, to);
            outEdges.add(edge);
            to.getInEdges().add(edge);
        }
    }

    /**
     * Basic Edge implementation.
     */
    @Beta
    public static class EdgeImpl implements Edge {
        private final Node from;
        private final Node to;

        public EdgeImpl(final Node from, final Node to) {
            this.from = from;
            this.to = to;
        }

        @Override
        public Node getFrom() {
            return from;
        }

        @Override
        public Node getTo() {
            return to;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Objects.hashCode(from);
            result = prime * result + Objects.hashCode(to);
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            EdgeImpl other = (EdgeImpl) obj;
            if (from == null) {
                if (other.from != null) {
                    return false;
                }
            } else if (!from.equals(other.from)) {
                return false;
            }
            if (to == null) {
                if (other.to != null) {
                    return false;
                }
            } else if (!to.equals(other.to)) {
                return false;
            }
            return true;
        }
    }

}
