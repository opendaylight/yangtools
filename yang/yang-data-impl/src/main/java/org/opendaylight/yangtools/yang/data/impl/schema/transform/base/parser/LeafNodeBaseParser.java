/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.AttributesBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

import com.google.common.base.Preconditions;

public abstract class LeafNodeBaseParser<E> implements
        ToNormalizedNodeParser<E, LeafNode<?>, LeafSchemaNode> {

    @Override
    public LeafNode<?> parse(List<E> elements, LeafSchemaNode schema) {
        Preconditions.checkArgument(elements.size() == 1, "Elements mapped to leaf node illegal count: %s", elements.size());
        Object value = parseLeaf(elements, schema);

        NormalizedNodeAttrBuilder<InstanceIdentifier.NodeIdentifier,Object,LeafNode<Object>> leafBuilder = Builders.leafBuilder(schema);

        leafBuilder.withAttributes(getAttributes(elements.get(0)));

        return leafBuilder.withValue(value).build();
    }

    protected abstract Object parseLeaf(List<E> elements, LeafSchemaNode schema);

    protected abstract Map<QName, String> getAttributes(E e);
}
