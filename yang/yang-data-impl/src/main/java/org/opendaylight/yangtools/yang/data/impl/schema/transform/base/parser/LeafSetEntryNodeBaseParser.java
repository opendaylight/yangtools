/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

/**
 * Abstract(base) parser for LeafSetEntryNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class LeafSetEntryNodeBaseParser<E> implements ExtensibleParser<NodeWithValue, E, LeafSetEntryNode<?>, LeafListSchemaNode> {

    private final BuildingStrategy<NodeWithValue, LeafSetEntryNode<?>> buildingStrategy;

    public LeafSetEntryNodeBaseParser() {
        buildingStrategy = new SimpleLeafSetEntryBuildingStrategy();
    }

    public LeafSetEntryNodeBaseParser(final BuildingStrategy<NodeWithValue, LeafSetEntryNode<?>> buildingStrategy) {
        this.buildingStrategy = buildingStrategy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final LeafSetEntryNode<?> parse(final Iterable<E> elements, final LeafListSchemaNode schema) {
        final int size = Iterables.size(elements);
        Preconditions.checkArgument(size == 1, "Xml elements mapped to leaf node illegal count: %s", size);

        final E e = elements.iterator().next();
        Object value = parseLeafListEntry(e,schema);

        NormalizedNodeAttrBuilder<NodeWithValue, Object, LeafSetEntryNode<Object>> leafEntryBuilder = Builders
                .leafSetEntryBuilder(schema);
        leafEntryBuilder.withAttributes(getAttributes(e));
        leafEntryBuilder.withValue(value);

        final BuildingStrategy rawBuildingStrat = buildingStrategy;
        return (LeafSetEntryNode<?>) rawBuildingStrat.build(leafEntryBuilder);
    }

    @Override
    public BuildingStrategy<NodeWithValue, LeafSetEntryNode<?>> getBuildingStrategy() {
        return buildingStrategy;
    }

    /**
     *
     * Parse the inner value of a LeafSetEntryNode from element of type E.
     *
     * @param element to be parsed
     * @param schema schema for leaf-list
     * @return parsed element as an Object
     */
    protected abstract Object parseLeafListEntry(E element, LeafListSchemaNode schema);

    /**
     *
     * @param e element to be parsed
     * @return attributes mapped to QNames
     */
    protected abstract Map<QName, String> getAttributes(E e);

    public static class SimpleLeafSetEntryBuildingStrategy implements BuildingStrategy<NodeWithValue, LeafSetEntryNode<?>> {

        @Override
        public LeafSetEntryNode<?> build(final NormalizedNodeBuilder<NodeWithValue, ?, LeafSetEntryNode<?>> builder) {
            return builder.build();
        }

        @Override
        public void prepareAttributes(final Map<QName, String> attributes, final NormalizedNodeBuilder<NodeWithValue, ?, LeafSetEntryNode<?>> containerBuilder) {
        }
    }
}
