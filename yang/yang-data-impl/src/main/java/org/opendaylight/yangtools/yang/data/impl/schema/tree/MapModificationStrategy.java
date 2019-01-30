/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import com.google.common.base.MoreObjects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.MapNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.NormalizedNodeContainerSupport.MapEntry;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

class MapModificationStrategy extends AbstractNodeContainerSupportModificationStrategy {
    private static final MapEntry<OrderedMapNode> ORDERED_SUPPORT = new MapEntry<>(OrderedMapNode.class,
            ImmutableOrderedMapNodeBuilder::create, ImmutableOrderedMapNodeBuilder::create);
    private static final MapEntry<MapNode> UNORDERED_SUPPORT = new MapEntry<>(MapNode.class,
            ImmutableMapNodeBuilder::create, ImmutableMapNodeBuilder::create);

    private static final class Ordered extends MapModificationStrategy {
        Ordered(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
            super(OrderedMapNode.class, ORDERED_SUPPORT, schema, treeConfig);
        }

        @Override
        protected ChildTrackingPolicy getChildPolicy() {
            return ChildTrackingPolicy.ORDERED;
        }
    }

    private final Optional<ModificationApplyOperation> entryStrategy;

    MapModificationStrategy(final Class<? extends MapNode> nodeClass, final MapEntry<?> support,
            final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(nodeClass, support, treeConfig);
        entryStrategy = Optional.of(ListEntryModificationStrategy.of(schema, treeConfig));
    }

    static AutomaticLifecycleMixin of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        final MapModificationStrategy strategy;
        final MapNode emptyNode;
        if (schema.isUserOrdered()) {
            strategy = new Ordered(schema, treeConfig);
            emptyNode = ImmutableNodes.orderedMapNode(schema.getQName());
        } else {
            strategy = new MapModificationStrategy(MapNode.class, UNORDERED_SUPPORT, schema, treeConfig);
            emptyNode = ImmutableNodes.mapNode(schema.getQName());
        }

        return new AutomaticLifecycleMixin(strategy, emptyNode);
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
    public final String toString() {
        return MoreObjects.toStringHelper(this).add("entry", entryStrategy.get()).toString();
    }
}
