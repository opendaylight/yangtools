/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserMapNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.AbstractNodeContainerModificationStrategy.Invisible;
import org.opendaylight.yangtools.yang.data.tree.impl.node.TreeNode;
import org.opendaylight.yangtools.yang.data.tree.impl.node.Version;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class MapModificationStrategy extends Invisible<ListSchemaNode> {
    private static final NormalizedNodeContainerSupport<NodeIdentifier, UserMapNode> ORDERED_SUPPORT =
            new NormalizedNodeContainerSupport<>(UserMapNode.class, ChildTrackingPolicy.ORDERED,
                    ImmutableUserMapNodeBuilder::create, ImmutableUserMapNodeBuilder::new);
    private static final NormalizedNodeContainerSupport<NodeIdentifier, SystemMapNode> UNORDERED_SUPPORT =
            new NormalizedNodeContainerSupport<>(SystemMapNode.class, ImmutableMapNodeBuilder::create,
                    ImmutableMapNodeBuilder::new);

    private final @NonNull MapNode emptyNode;

    private MapModificationStrategy(final NormalizedNodeContainerSupport<?, ?> support, final ListSchemaNode schema,
        final DataTreeConfiguration treeConfig, final MapNode emptyNode) {
        super(support, treeConfig, MapEntryModificationStrategy.of(schema, treeConfig));
        this.emptyNode = requireNonNull(emptyNode);
    }

    static MapModificationStrategy of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        final NormalizedNodeContainerSupport<?, ?> support;
        final MapNode emptyNode;
        if (schema.isUserOrdered()) {
            support = ORDERED_SUPPORT;
            emptyNode = ImmutableNodes.orderedMapNode(schema.getQName());
        } else {
            support = UNORDERED_SUPPORT;
            emptyNode = ImmutableNodes.mapNode(schema.getQName());
        }
        return new MapModificationStrategy(support, schema, treeConfig, emptyNode);
    }

    @Override
    public ModificationApplyOperation childByArg(final PathArgument arg) {
        return arg instanceof NodeIdentifierWithPredicates ? entryStrategy() : null;
    }

    @Override
    TreeNode apply(final ModifiedNode modification, final TreeNode currentMeta, final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, currentMeta,
            version);
    }

    @Override
    TreeNode defaultTreeNode() {
        return defaultTreeNode(emptyNode);
    }
}
