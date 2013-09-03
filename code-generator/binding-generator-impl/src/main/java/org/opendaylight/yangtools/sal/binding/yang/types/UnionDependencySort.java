/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.binding.yang.types;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.ExtendedType;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort;
import org.opendaylight.yangtools.yang.parser.util.TopologicalSort.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class UnionDependencySort {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnionDependencySort.class);

    /**
     * Sorts union types by mutual dependencies.
     * 
     * At the beginning the union types are selected from
     * <code>typeDefinitions</code> and wrapped to nodes. The nodes are sorted
     * and then the wrapped payload is extracted.
     * 
     * @param typeDefinitions
     *            set of type definitions.
     * @return list of extended type which are sorted by mutual dependencies
     * @throws IllegalArgumentException
     *             if <code>typeDefinitions</code> equals <code>null</code>
     */
    public List<ExtendedType> sort(final Set<TypeDefinition<?>> typeDefinitions) {
        if (typeDefinitions == null) {
            LOGGER.error("Set of Type Definitions cannot be NULL!");
            throw new IllegalArgumentException("Set of Type Definitions " + "cannot be NULL!");
        }

        final Set<ExtendedType> extUnionTypes = unionsFromTypeDefinitions(typeDefinitions);

        final Set<Node> unsorted = unionTypesToNodes(extUnionTypes);

        final List<Node> sortedNodes = TopologicalSort.sort(unsorted);
        return Lists.transform(sortedNodes, new Function<Node, ExtendedType>() {
            @Override
            public ExtendedType apply(Node input) {
                return (ExtendedType) (((NodeWrappedType) input).getWrappedType());
            }
        });
    }

    /**
     * Extracts only union types from <code>typeDefinitions</code> set.
     * 
     * @param typeDefinitions
     *            set of all type definitions
     * @return set of extended type which are union type definition
     */
    private Set<ExtendedType> unionsFromTypeDefinitions(final Set<TypeDefinition<?>> typeDefinitions) {
        final Set<ExtendedType> unions = Sets.newHashSet();

        for (final TypeDefinition<?> typedef : typeDefinitions) {
            if ((typedef != null) && (typedef.getBaseType() != null) && (typedef instanceof ExtendedType)
                    && (typedef.getBaseType() instanceof UnionTypeDefinition)) {
                unions.add((ExtendedType) typedef);
            }
        }
        return unions;
    }

    /**
     * Wraps every extended type which represents union to node type and adds to
     * every node information about dependencies.
     * 
     * The mapping from union type to node is created. For every created node
     * (next <i>nodeFrom</i>) is for its wrapped union type passed the list of
     * inner types through and only those inner types which represent union type
     * are next considered. For every inner union type is found its wrapping
     * node (next as <i>nodeTo</i>). This dependency relationship between
     * nodeFrom and all found nodesTo is modeled with creating of one edge from
     * nodeFrom to nodeTo.
     * 
     * 
     * @param extUnionTypes
     *            set of extended types which represents union types
     * @return set of nodes which contains wrapped union types set of node where
     *         each one contains wrapped one union type
     */
    private Set<Node> unionTypesToNodes(final Set<ExtendedType> extUnionTypes) {
        final Map<ExtendedType, Node> nodeMap = Maps.newHashMap();
        final Set<Node> resultNodes = Sets.newHashSet();

        for (final ExtendedType unionType : extUnionTypes) {
            final Node node = new NodeWrappedType(unionType);
            nodeMap.put(unionType, node);
            resultNodes.add(node);
        }

        for (final Node node : resultNodes) {
            final NodeWrappedType nodeFrom = (NodeWrappedType) node;
            final ExtendedType extUnionType = (ExtendedType) nodeFrom.getWrappedType();

            final UnionTypeDefinition unionType = (UnionTypeDefinition) extUnionType.getBaseType();

            final List<TypeDefinition<?>> innerTypes = unionType.getTypes();
            for (final TypeDefinition<?> typedef : innerTypes) {
                if (extUnionTypes.contains(typedef)) {
                    final Node toNode = nodeMap.get(typedef);
                    nodeFrom.addEdge(toNode);
                }
            }
        }

        return resultNodes;
    }

}
