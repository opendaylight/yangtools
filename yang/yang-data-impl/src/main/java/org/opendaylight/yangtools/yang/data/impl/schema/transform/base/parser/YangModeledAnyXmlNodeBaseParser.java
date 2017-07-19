/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.Collection;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.YangModeledAnyXmlNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.YangModeledAnyXmlSchemaNode;

/**
 * Abstract(base) parser for yang modeled anyXml nodes, parses elements of type E.
 *
 * @param <E>
 *            type of elements to be parsed
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class YangModeledAnyXmlNodeBaseParser<E> implements
        ToNormalizedNodeParser<E, YangModeledAnyXmlNode, YangModeledAnyXmlSchemaNode> {

    @Override
    public final YangModeledAnyXmlNode parse(final Iterable<E> elements, final YangModeledAnyXmlSchemaNode schema) {
        final int size = Iterables.size(elements);
        Preconditions.checkArgument(size == 1, "Elements mapped to yang modeled any-xml node illegal count: %s", size);

        final E e = elements.iterator().next();
        final Collection<DataContainerChild<? extends PathArgument, ?>> value = parseAnyXml(e, schema);

        final DataContainerNodeAttrBuilder<NodeIdentifier, YangModeledAnyXmlNode> yangModeledAnyXmlBuilder = Builders
                .yangModeledAnyXmlBuilder(schema);

        return yangModeledAnyXmlBuilder.withValue(value).build();
    }

    /**
     *
     * Parse the inner value of an YangModeledAnyXmlNode from element of type E.
     *
     * @param element
     *            to be parsed
     * @param yangModeledAnyXmlSchemaNode
     *            schema for leaf
     * @return parsed element as an Object
     */
    protected abstract Collection<DataContainerChild<? extends PathArgument, ?>> parseAnyXml(E element, YangModeledAnyXmlSchemaNode yangModeledAnyXmlSchemaNode);

}
