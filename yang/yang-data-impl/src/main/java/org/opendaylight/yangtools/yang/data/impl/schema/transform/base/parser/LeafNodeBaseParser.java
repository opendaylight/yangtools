/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

/**
 * Abstract(base) parser for LeafNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class LeafNodeBaseParser<E> implements
        ToNormalizedNodeParser<E, LeafNode<?>, LeafSchemaNode> {

    @Override
    public final LeafNode<?> parse(Iterable<E> elements, LeafSchemaNode schema) {
        final int size = Iterables.size(elements);
        Preconditions.checkArgument(size == 1, "Elements mapped to leaf node illegal count: %s", size);
        Object value = parseLeaf(elements.iterator().next(), schema);
        return Builders.leafBuilder(schema).withValue(value).build();
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
}
