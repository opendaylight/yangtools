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
 * @param <T> value type of elements being parsed
 */
public abstract class LeafSetEntryNodeBaseParser<E, T> implements ExtensibleParser<NodeWithValue<T>, E, LeafSetEntryNode<T>, LeafListSchemaNode> {

    private final BuildingStrategy<NodeWithValue<T>, LeafSetEntryNode<T>> buildingStrategy;

    public LeafSetEntryNodeBaseParser() {
        buildingStrategy = new SimpleLeafSetEntryBuildingStrategy<>();
    }

    public LeafSetEntryNodeBaseParser(final BuildingStrategy<NodeWithValue<T>, LeafSetEntryNode<T>> buildingStrategy) {
        this.buildingStrategy = buildingStrategy;
    }

    @SuppressWarnings("unchecked")
    @Override
    public final LeafSetEntryNode<T> parse(Iterable<E> elements, LeafListSchemaNode schema) {
        final int size = Iterables.size(elements);
        Preconditions.checkArgument(size == 1, "Xml elements mapped to leaf node illegal count: %s", size);

        final E e = elements.iterator().next();
        final T value = (T) parseLeafListEntry(e, schema);

        NormalizedNodeAttrBuilder<NodeWithValue<T>, T, LeafSetEntryNode<T>> leafEntryBuilder =
                Builders.leafSetEntryBuilder(schema);
        leafEntryBuilder.withAttributes(getAttributes(e));
        leafEntryBuilder.withValue(value);

        return buildingStrategy.build(leafEntryBuilder);
    }

    @Override
    public BuildingStrategy<NodeWithValue<T>, LeafSetEntryNode<T>> getBuildingStrategy() {
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

    public static class SimpleLeafSetEntryBuildingStrategy<T> implements BuildingStrategy<YangInstanceIdentifier.NodeWithValue<T>, LeafSetEntryNode<T>> {

        @Override
        public LeafSetEntryNode<T> build(final NormalizedNodeBuilder<NodeWithValue<T>, ?, LeafSetEntryNode<T>> builder) {
            return builder.build();
        }

        @Override
        public void prepareAttributes(final Map<QName, String> attributes, final NormalizedNodeBuilder<NodeWithValue<T>, ?, LeafSetEntryNode<T>> containerBuilder) {
        }
    }
}
