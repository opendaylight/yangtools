/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataValidationFailedException;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.TreeNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.spi.Version;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class OrderedMapModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final Optional<ModificationApplyOperation> entryStrategy;
    private final OrderedMapNode emptyNode;

    OrderedMapModificationStrategy(final ListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(OrderedMapNode.class, treeConfig);
        entryStrategy = Optional.of(new ListEntryModificationStrategy(schema, treeConfig));
        emptyNode = ImmutableNodes.orderedMapNode(schema.getQName());
    }

    @Override
    Optional<TreeNode> apply(final ModifiedNode modification, final Optional<TreeNode> storeMeta,
            final Version version) {
        return AutomaticLifecycleMixin.apply(super::apply, emptyNode, modification, storeMeta, version);
    }

    @Override
    void checkApplicable(final ModificationPath path, final NodeModification modification,
            final Optional<TreeNode> current, final Version version) throws DataValidationFailedException {
        AutomaticLifecycleMixin.checkApplicable(super::checkApplicable, emptyNode, path, modification, current,
            version);
    }

    @Override
    protected ChildTrackingPolicy getChildPolicy() {
        return ChildTrackingPolicy.ORDERED;
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected NormalizedNodeContainerBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof OrderedMapNode);
        return ImmutableOrderedMapNodeBuilder.create((OrderedMapNode) original);
    }

    @Override
    protected NormalizedNode<?, ?> createEmptyValue(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof OrderedMapNode);
        return ImmutableOrderedMapNodeBuilder.create().withNodeIdentifier(((OrderedMapNode) original).getIdentifier())
                .build();
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument identifier) {
        return identifier instanceof NodeIdentifierWithPredicates ? entryStrategy : Optional.empty();
    }

    @Override
    public String toString() {
        return "OrderedMapModificationStrategy [entry=" + entryStrategy + "]";
    }
}