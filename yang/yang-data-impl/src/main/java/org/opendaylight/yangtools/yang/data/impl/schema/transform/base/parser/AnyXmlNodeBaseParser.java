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
import org.opendaylight.yangtools.yang.data.api.Node;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.AnyXmlSchemaNode;

/**
 * Abstract(base) parser for LeafNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 */
public abstract class AnyXmlNodeBaseParser<E> implements
        ToNormalizedNodeParser<E, AnyXmlNode, AnyXmlSchemaNode> {

    @Override
    public final AnyXmlNode parse(Iterable<E> elements, AnyXmlSchemaNode schema) {
        final int size = Iterables.size(elements);
        Preconditions.checkArgument(size == 1, "Elements mapped to any-xml node illegal count: %s", size);

        final E e = elements.iterator().next();
        Node<?> value = parseAnyXml(e, schema);

        NormalizedNodeAttrBuilder<NodeIdentifier, Node<?>, AnyXmlNode> anyXmlBuilder = Builders.anyXmlBuilder(schema);

        return anyXmlBuilder.withValue(value).build();
    }

    /**
     *
     * Parse the inner value of a AnyXmlNode from element of type E.
     *
     * @param element to be parsed
     * @param schema schema for leaf
     * @return parsed element as an Object
     */
    protected abstract Node<?> parseAnyXml(E element, AnyXmlSchemaNode schema);

}
