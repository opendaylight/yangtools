/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.MapEntry;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class MapModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private static final MapEntry<OrderedMapNode> ORDERED_SUPPORT = new MapEntry<>(OrderedMapNode.class,
            ImmutableOrderedMapNodeBuilder::create, ImmutableOrderedMapNodeBuilder::create);
    private static final MapEntry<MapNode> UNORDERED_SUPPORT = new MapEntry<>(MapNode.class,
            ImmutableMapNodeBuilder::create, ImmutableMapNodeBuilder::create);

    private static final class Ordered extends MapModificationStrategy {
        Ordered(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
            super(ORDERED_SUPPORT, schema, treeConfig, ImmutableNodes.orderedMapNode(schema.getQName()));
        }

        @Override
        protected ChildTrackingPolicy getChildPolicy() {
            return ChildTrackingPolicy.ORDERED;
        }
    }

    private final Optional<ModificationApplyOperation> entryStrategy;
    private final MapNode emptyNode;

    MapModificationStrategy(final MapEntry<?> support, final ListSchemaNode schema,
        final DataTreeConfiguration treeConfig, final MapNode emptyNode) {
        super(support, treeConfig);
        this.emptyNode = requireNonNull(emptyNode);
        entryStrategy = Optional.of(ListEntryModificationStrategy.of(schema, treeConfig));
    }

    static MapModificationStrategy of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        return schema.isUserOrdered() ?  new Ordered(schema, treeConfig)
                : new MapModificationStrategy(UNORDERED_SUPPORT, schema, treeConfig,
                    ImmutableNodes.mapNode(schema.getQName()));
    }

    // FIXME: this is a hack, originally introduced in
    //        Change-Id: I9dc02a1917f38e8a0d62279843974b9869c48693. DataTreeRoot needs to be fixed up to properly
    //        handle the lookup of through maps.
    @Override
    public final Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
        if (identifier instanceof NodeIdentifierWithPredicates) {
            return entryStrategy;
        }
        // In case we already are in a MapEntry node(for example DataTree rooted at MapEntry)
        // try to retrieve the child that the identifier should be pointing to from our entryStrategy
        // if we have one. If the entryStrategy cannot find this child we just return the absent
        // we get from it.
        return entryStrategy.get().getChild(identifier);
    }

    @Override
    final Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, storeMeta,
            version);
    }

    @Override
    final void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        AutomaticLifecycleMixin.checkApplicable(super::checkApplicable, emptyNode, path, modification, current,
            version);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("entry", entryStrategy.get()).toString();
    }
}
