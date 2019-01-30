/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.MapEntry;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class OrderedMapModificationStrategy extends AbstractMapModificationStrategy {
    private static final MapEntry<OrderedMapNode> SUPPORT = new MapEntry<>(OrderedMapNode.class,
            ImmutableOrderedMapNodeBuilder::create, ImmutableOrderedMapNodeBuilder::create);

    OrderedMapModificationStrategy(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(OrderedMapNode.class, SUPPORT, schema, treeConfig);
    }

    static AutomaticLifecycleMixin of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        return new AutomaticLifecycleMixin(new OrderedMapModificationStrategy(schema, treeConfig),
            ImmutableNodes.orderedMapNode(schema.getQName()));
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return ChildTrackingPolicy.ORDERED;
    }
}
