/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.transform.base.parser;

import java.util.Collections;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.Builders;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.ListNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.transform.ToNormalizedNodeParser;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

/**
 * Abstract(base) parser for LeafSetNodes, parses elements of type E.
 *
 * @param <E> type of elements to be parsed
 * @deprecated Use yang-data-codec-xml instead.
 */
@Deprecated
public abstract class LeafSetNodeBaseParser<E> implements
        ToNormalizedNodeParser<E, LeafSetNode<?>, LeafListSchemaNode> {

    @Override
    public final LeafSetNode<?> parse(final Iterable<E> childNodes, final LeafListSchemaNode schema) {

        ListNodeBuilder<Object, LeafSetEntryNode<Object>> leafListBuilder =
          schema.isUserOrdered() ? Builders.orderedLeafSetBuilder(schema) : Builders.leafSetBuilder(schema);
        for (E childNode : childNodes) {
            LeafSetEntryNode<?> builtChild = getLeafSetEntryNodeParser().parse(
                    Collections.singletonList(childNode), schema);

            // TODO: can we get rid of this cast/SuppressWarnings somehow?
            @SuppressWarnings("unchecked")
            final LeafSetEntryNode<Object> child = (LeafSetEntryNode<Object>) builtChild;
            leafListBuilder.withChild(child);
        }

        return leafListBuilder.build();
    }

    /**
     *
     * @return parser for inner LeafSetEntryNodes used to parse every entry of LeafSetNode, might be the same instance in case its immutable
     */
    protected abstract ToNormalizedNodeParser<E, LeafSetEntryNode<?>, LeafListSchemaNode> getLeafSetEntryNodeParser();
}
