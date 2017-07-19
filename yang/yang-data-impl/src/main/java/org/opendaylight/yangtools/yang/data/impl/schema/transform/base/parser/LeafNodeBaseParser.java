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
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.util.LeafInterner;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

/**
 * Abstract(base) parser for LeafNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class LeafNodeBaseParser<E> implements ExtensibleParser<NodeIdentifier, E, LeafNode<?>, LeafSchemaNode> {

    private final BuildingStrategy<NodeIdentifier, LeafNode<?>> buildingStrategy;

    public LeafNodeBaseParser() {
        buildingStrategy = new SimpleLeafBuildingStrategy();
    }

    public LeafNodeBaseParser(final BuildingStrategy<NodeIdentifier, LeafNode<?>> buildingStrategy) {
        this.buildingStrategy = buildingStrategy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final LeafNode<?> parse(final Iterable<E> elements, final LeafSchemaNode schema) {
        final int size = Iterables.size(elements);
        Preconditions.checkArgument(size == 1, "Elements mapped to leaf node illegal count: %s", size);

        final E e = elements.iterator().next();
        Object value = parseLeaf(e, schema);

        NormalizedNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifier, Object, LeafNode<Object>> leafBuilder =
                Builders.leafBuilder(schema);

        leafBuilder.withAttributes(getAttributes(e));

        @SuppressWarnings("rawtypes")
        final BuildingStrategy rawBuildingStrat = buildingStrategy;
        final LeafNode<?> sample = (LeafNode<?>)rawBuildingStrat.build(leafBuilder.withValue(value));
        return sample == null ? null : LeafInterner.forSchema(schema).intern(sample);
    }

    /**
     *
     * Parse the inner value of a LeafNode from element of type E.
     *
     * @param element to be parsed
     * @param schema schema for leaf
     * @return parsed element as an Object
     */
    protected abstract Object parseLeaf(E element, LeafSchemaNode schema);

    /**
     *
     * @param e element to be parsed
     * @return attributes mapped to QNames
     */
    protected abstract Map<QName, String> getAttributes(E e);

    @Override
    public BuildingStrategy<NodeIdentifier, LeafNode<?>> getBuildingStrategy() {
        return buildingStrategy;
    }

    public static class SimpleLeafBuildingStrategy implements BuildingStrategy<NodeIdentifier, LeafNode<?>> {
        @Override
        public LeafNode<?> build(final NormalizedNodeBuilder<NodeIdentifier, ?, LeafNode<?>> builder) {
            return builder.build();
        }

        @Override
        public void prepareAttributes(final Map<QName, String> attributes, final NormalizedNodeBuilder<NodeIdentifier, ?, LeafNode<?>> containerBuilder) {
            // NOOP
        }
    }
}
