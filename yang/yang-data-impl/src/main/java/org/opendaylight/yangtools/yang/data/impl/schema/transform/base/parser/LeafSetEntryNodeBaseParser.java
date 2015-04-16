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
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

/**
 * Abstract(base) parser for LeafSetEntryNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class LeafSetEntryNodeBaseParser<E> implements
        ToNormalizedNodeParser<E, LeafSetEntryNode<?>, LeafListSchemaNode> {

    private static final LeafStrategy DEFAULT_PARSING_STRATEGY = new LeafStrategy<NodeWithValue, Object, LeafSetEntryNode<Object>>() {

        @Override
        public LeafSetEntryNode<Object> applyStrategy(NormalizedNodeAttrBuilder<NodeWithValue, Object, LeafSetEntryNode<Object>> builder) {
            return builder.build();
        }
    };

    @Override
    public final LeafSetEntryNode<?> parse(Iterable<E> elements, LeafListSchemaNode schema) {
        final int size = Iterables.size(elements);
        Preconditions.checkArgument(size == 1, "Xml elements mapped to leaf node illegal count: %s", size);

        final E e = elements.iterator().next();
        Object value = parseLeafListEntry(e,schema);

        NormalizedNodeAttrBuilder<YangInstanceIdentifier.NodeWithValue, Object, LeafSetEntryNode<Object>> leafEntryBuilder = Builders
                .leafSetEntryBuilder(schema);
        leafEntryBuilder.withAttributes(getAttributes(e));
        leafEntryBuilder.withValue(value);

        return getParsingStrategy().applyStrategy(leafEntryBuilder);
    }

    protected LeafStrategy<NodeWithValue, Object, LeafSetEntryNode<Object>> getParsingStrategy() {
        return DEFAULT_PARSING_STRATEGY;
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
     * @param e
     * @return attributes mapped to QNames
     */
    protected abstract Map<QName, String> getAttributes(E e);

}
