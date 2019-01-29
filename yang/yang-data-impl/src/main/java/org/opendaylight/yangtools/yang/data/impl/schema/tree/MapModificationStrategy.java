/*
 * Copyright (c) 2019 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
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

final class MapModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private static final MapEntry<OrderedMapNode> ORDERED_SUPPORT = new MapEntry<>(OrderedMapNode.class,
            ChildTrackingPolicy.ORDERED, ImmutableOrderedMapNodeBuilder::create,
            ImmutableOrderedMapNodeBuilder::create);
    private static final MapEntry<MapNode> UNORDERED_SUPPORT = new MapEntry<>(MapNode.class,
            ChildTrackingPolicy.UNORDERED, ImmutableMapNodeBuilder::create, ImmutableMapNodeBuilder::create);

    private final Optional<ModificationApplyOperation> entryStrategy;
    private final MapNode emptyNode;

    private MapModificationStrategy(final MapEntry<?> support, final ListSchemaNode schema,
        final DataTreeConfiguration treeConfig, final MapNode emptyNode) {
        super(support, treeConfig);
        this.emptyNode = requireNonNull(emptyNode);
        entryStrategy = Optional.of(ListEntryModificationStrategy.of(schema, treeConfig));
    }

    static MapModificationStrategy of(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        final MapEntry<?> support;
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

    // FIXME: this is a hack, originally introduced in
    //        Change-Id: I9dc02a1917f38e8a0d62279843974b9869c48693. DataTreeRoot needs to be fixed up to properly
    //        handle the lookup of through maps.
    @Override
    public Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
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
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, this::applyWrite, emptyNode, modification, storeMeta,
            version);
    }

    @Override
    void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        AutomaticLifecycleMixin.checkApplicable(super::checkApplicable, emptyNode, path, modification, current,
            version);
    }

    @Override
    ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return super.addToStringAttributes(helper).add("entry", entryStrategy.get());
    }
}
