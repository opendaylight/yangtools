/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.AbstractNodeContainerModificationStrategy.Invisible;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class MapModificationStrategy extends Invisible<ListSchemaNode> {
    private static final NormalizedNodeContainerSupport<NodeIdentifier, OrderedMapNode> ORDERED_SUPPORT =
            new NormalizedNodeContainerSupport<>(OrderedMapNode.class, ChildTrackingPolicy.ORDERED,
                    ImmutableOrderedMapNodeBuilder::create, ImmutableOrderedMapNodeBuilder::create);
    private static final NormalizedNodeContainerSupport<NodeIdentifier, MapNode> UNORDERED_SUPPORT =
            new NormalizedNodeContainerSupport<>(MapNode.class, ImmutableMapNodeBuilder::create,
                    ImmutableMapNodeBuilder::create);

    private final @NonNull MapNode emptyNode;

    private MapModificationStrategy(final NormalizedNodeContainerSupport<?, ?> support, final ListSchemaNode schema,
        final DataTreeConfiguration treeConfig, final MapNode emptyNode) {
        super(support, treeConfig, ListEntryModificationStrategy.of(schema, treeConfig));
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
    public Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
        return identifier instanceof NodeIdentifierWithPredicates ? entryStrategy() : Optional.empty();
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, storeMeta,
            version);
    }

    @Override
    TreeNode defaultTreeNode() {
        return defaultTreeNode(emptyNode);
    }
}
