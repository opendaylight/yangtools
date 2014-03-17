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
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

import com.google.common.base.Preconditions;

public abstract class LeafSetEntryNodeBaseParser<E> implements
        ToNormalizedNodeParser<E, LeafSetEntryNode<?>, LeafListSchemaNode> {

    @Override
    public LeafSetEntryNode<Object> parse(List<E> elements, LeafListSchemaNode schema) {
        Preconditions.checkArgument(elements.size() == 1, "Xml elements mapped to leaf node illegal count: %s",
                elements.size());
        Object value = parseLeafListEntry(elements, schema);

        NormalizedNodeAttrBuilder<InstanceIdentifier.NodeWithValue, Object, LeafSetEntryNode<Object>> leafEntryBuilder = Builders
                .leafSetEntryBuilder(schema);
        leafEntryBuilder.withAttributes(getAttributes(elements.get(0)));

        return leafEntryBuilder.withValue(value).build();
    }

    protected abstract Object parseLeafListEntry(List<E> elements, LeafListSchemaNode schema);

    protected abstract Map<QName, String> getAttributes(E e);

}
