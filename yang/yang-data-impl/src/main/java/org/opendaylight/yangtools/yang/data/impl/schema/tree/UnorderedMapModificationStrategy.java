/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.MapEntry;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class UnorderedMapModificationStrategy extends AbstractMapModificationStrategy {
    private static final MapEntry<MapNode> SUPPORT = new MapEntry<>(MapNode.class,
            ImmutableMapNodeBuilder::create, ImmutableMapNodeBuilder::create);

    UnorderedMapModificationStrategy(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(MapNode.class, SUPPORT, schema, treeConfig);
    }

    static AutomaticLifecycleMixin of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        return new AutomaticLifecycleMixin(new UnorderedMapModificationStrategy(schema, treeConfig),
            ImmutableNodes.mapNode(schema.getQName()));
    }
}
