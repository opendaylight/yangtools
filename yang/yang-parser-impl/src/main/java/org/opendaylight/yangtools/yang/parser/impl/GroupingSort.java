/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataNodeContainerBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.DataSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.util.NodeWrappedType;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.Node;

public final class GroupingSort {
    private GroupingSort() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    /**
     * Sorts set <code>groupingDefinitions</code> according to the mutual
     * dependencies.<br>
     *
     * Elements of <code>groupingDefinitions</code> are firstly transformed to
     * {@link org.opendaylight.yangtools.yang.parser.util.TopologicalSort.Node
     * Node} interfaces and then are sorted by
     * {@link org.opendaylight.yangtools.yang.parser.util.TopologicalSort#sort(Set)
     * sort()} method of <code>TopologicalSort</code>.<br>
     * <br>
     *
     *
     * <i>Definition of dependency relation:<br>
     * The first <code>GroupingDefinition</code> object (in this context)
     * depends on second <code>GroupingDefinition</code> object if the first one
     * contains in its set of <code>UsesNode</code> (obtained through
     * {@link org.opendaylight.yangtools.yang.model.api.DataNodeContainer#getUses()
     * getUses} method) reference to the second one.</i>
     *
     * @param groupingDefinitions
     *            set of grouping definition which should be sorted according to
     *            mutual dependencies
     * @return list of grouping definitions which are sorted by mutual
     *         dependencies
     * @throws IllegalArgumentException
     *             if <code>groupingDefinitions</code>
     *
     */
    public static List<GroupingBuilder> sort(final Collection<GroupingBuilder> groupingDefinitions) {
        if (groupingDefinitions == null) {
            throw new IllegalArgumentException("Set of Type Definitions " + "cannot be NULL!");
        }

        final List<GroupingBuilder> resultGroupingDefinitions = new ArrayList<>();
        final Set<Node> unsorted = groupingDefinitionsToNodes(groupingDefinitions);
        final List<Node> sortedNodes = TopologicalSort.sort(unsorted);
        for (Node node : sortedNodes) {
            NodeWrappedType nodeWrappedType = (NodeWrappedType) node;
            resultGroupingDefinitions.add((GroupingBuilder) (nodeWrappedType.getWrappedType()));
        }
        return resultGroupingDefinitions;

    }

    /**
     * Wraps every grouping definition to node type and adds to every node
     * information about dependencies.
     *
     * The map with mapping from schema path (represents grouping definition) to
     * node is created. For every created node (next <i>nodeFrom</i>) is for its
     * wrapped grouping definition passed the set of its <i>uses nodes</i>
     * through. For every uses node is found its wrapping node (next as
     * <i>nodeTo</i>). This dependency relationship between nodeFrom and all
     * found nodesTo is modeled with creating of one edge from nodeFrom to
     * nodeTo.
     *
     *
     * @param groupingDefinitions
     *            set of goruping definition which will be wrapped to nodes
     *
     * @return set of nodes where every one contains wrapped grouping definition
     */
    private static Set<Node> groupingDefinitionsToNodes(final Collection<GroupingBuilder> groupingDefinitions) {
        final Map<SchemaPath, Node> nodeMap = Maps.newHashMap();
        final Set<Node> resultNodes = Sets.newHashSet();

        for (final GroupingBuilder groupingDefinition : groupingDefinitions) {
            final Node node = new NodeWrappedType(groupingDefinition);
            nodeMap.put(groupingDefinition.getPath(), node);
            resultNodes.add(node);
        }

        for (final Node node : resultNodes) {
            final NodeWrappedType nodeWrappedType = (NodeWrappedType) node;
            final GroupingBuilder groupingDefinition = (GroupingBuilder) nodeWrappedType.getWrappedType();

            Set<UsesNodeBuilder> usesNodes = getAllUsesNodes(groupingDefinition);
            for (UsesNodeBuilder usesNode : usesNodes) {
                SchemaPath schemaPath = usesNode.getGroupingBuilder().getPath();
                Node nodeTo = nodeMap.get(schemaPath);
                if (nodeTo == null) {
                    throw new IllegalArgumentException("target grouping not found for uses " + usesNode);
                }
                nodeWrappedType.addEdge(nodeTo);
            }
        }

        return resultNodes;
    }

    /**
     * Returns the set of the uses nodes which are get from uses in
     * <code>container</code>, from uses in groupings inside
     * <code>container</code> and from uses inside child nodes of the
     * <code>container</code>.
     *
     * @param container
     *            data node container which can contain some uses of grouping
     * @return set of uses nodes which were find in <code>container</code>.
     */
    public static Set<UsesNodeBuilder> getAllUsesNodes(final DataNodeContainerBuilder container) {
        Set<UsesNodeBuilder> ret = new HashSet<>();
        List<UsesNodeBuilder> usesNodes = container.getUsesNodeBuilders();
        ret.addAll(usesNodes);

        for (UsesNodeBuilder usesNode : usesNodes) {
            for (AugmentationSchemaBuilder augment : usesNode.getAugmentations()) {
                ret.addAll(getAllUsesNodes(augment));
            }
        }
        Set<GroupingBuilder> groupings = container.getGroupingBuilders();
        for (GroupingBuilder groupingDefinition : groupings) {
            ret.addAll(getAllUsesNodes(groupingDefinition));
        }
        List<DataSchemaNodeBuilder> childNodes = container.getChildNodeBuilders();
        for (DataSchemaNodeBuilder childNode : childNodes) {
            if (childNode instanceof DataNodeContainerBuilder) {
                ret.addAll(getAllUsesNodes((DataNodeContainerBuilder) childNode));
            } else if (childNode instanceof ChoiceBuilder) {
                Set<ChoiceCaseBuilder> cases = ((ChoiceBuilder) childNode).getCases();
                for (ChoiceCaseBuilder choiceCaseNode : cases) {
                    ret.addAll(getAllUsesNodes(choiceCaseNode));
                }
            }
        }
        return ret;
    }

}
