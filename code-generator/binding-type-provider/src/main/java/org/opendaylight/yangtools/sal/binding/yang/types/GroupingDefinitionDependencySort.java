/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.opendaylight.yangtools.yang.model.api.AugmentationSchema;
import org.opendaylight.yangtools.yang.model.api.ChoiceCaseNode;
import org.opendaylight.yangtools.yang.model.api.ChoiceSchemaNode;
import org.opendaylight.yangtools.yang.model.api.DataNodeContainer;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.GroupingDefinition;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.UsesNode;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.Node;

public class GroupingDefinitionDependencySort {

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
     * @return list of grouping definitiond which are sorted by mutual
     *         dependencies
     * @throws IllegalArgumentException
     *             if <code>groupingDefinitions</code>
     *
     */
    public List<GroupingDefinition> sort(final Collection<GroupingDefinition> groupingDefinitions) {
        if (groupingDefinitions == null) {
            throw new IllegalArgumentException("Set of Type Definitions " + "cannot be NULL!");
        }

        final List<GroupingDefinition> resultGroupingDefinitions = new ArrayList<GroupingDefinition>();
        final Set<Node> unsorted = groupingDefinitionsToNodes(groupingDefinitions);
        final List<Node> sortedNodes = TopologicalSort.sort(unsorted);
        for (Node node : sortedNodes) {
            NodeWrappedType nodeWrappedType = (NodeWrappedType) node;
            resultGroupingDefinitions.add((GroupingDefinition) (nodeWrappedType.getWrappedType()));
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
    private Set<Node> groupingDefinitionsToNodes(final Collection<GroupingDefinition> groupingDefinitions) {
        final Map<SchemaPath, Node> nodeMap = Maps.newHashMap();
        final Set<Node> resultNodes = Sets.newHashSet();

        for (final GroupingDefinition groupingDefinition : groupingDefinitions) {
            final Node node = new NodeWrappedType(groupingDefinition);
            nodeMap.put(groupingDefinition.getPath(), node);
            resultNodes.add(node);
        }

        for (final Node node : resultNodes) {
            final NodeWrappedType nodeWrappedType = (NodeWrappedType) node;
            final GroupingDefinition groupingDefinition = (GroupingDefinition) nodeWrappedType.getWrappedType();

            Set<UsesNode> usesNodes = getAllUsesNodes(groupingDefinition);

            for (UsesNode usesNode : usesNodes) {
                SchemaPath schemaPath = usesNode.getGroupingPath();
                if (schemaPath != null) {
                    Node nodeTo = nodeMap.get(schemaPath);
                    if (nodeTo != null) {
                        nodeWrappedType.addEdge(nodeTo);
                    }
                }
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
    private Set<UsesNode> getAllUsesNodes(final DataNodeContainer container) {
        Set<UsesNode> ret = new HashSet<>();
        Set<UsesNode> usesNodes = container.getUses();
        ret.addAll(usesNodes);

        for (UsesNode usesNode : usesNodes) {
            for (AugmentationSchema augment : usesNode.getAugmentations()) {
                ret.addAll(getAllUsesNodes(augment));
            }
        }
        Set<GroupingDefinition> groupings = container.getGroupings();
        for (GroupingDefinition groupingDefinition : groupings) {
            ret.addAll(getAllUsesNodes(groupingDefinition));
        }
        for (DataSchemaNode childNode : container.getChildNodes()) {
            if (childNode instanceof DataNodeContainer) {
                ret.addAll(getAllUsesNodes((DataNodeContainer) childNode));
            } else if (childNode instanceof ChoiceSchemaNode) {
                Set<ChoiceCaseNode> cases = ((ChoiceSchemaNode) childNode).getCases();
                for (ChoiceCaseNode choiceCaseNode : cases) {
                    ret.addAll(getAllUsesNodes(choiceCaseNode));
                }
            }
        }
        return ret;
    }

}
