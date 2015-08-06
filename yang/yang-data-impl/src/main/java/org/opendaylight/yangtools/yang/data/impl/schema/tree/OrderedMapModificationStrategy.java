/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedMapNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedMapNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;

final class OrderedMapModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final Optional<ModificationApplyOperation> entryStrategy;

    OrderedMapModificationStrategy(final ListSchemaNode schema, final TreeType treeType) {
        super(OrderedMapNode.class, treeType);
        entryStrategy = Optional.<ModificationApplyOperation> of(new ListEntryModificationStrategy(schema, treeType));
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
    public Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
        if (identifier instanceof YangInstanceIdentifier.NodeIdentifierWithPredicates) {
            return entryStrategy;
        }
        return Optional.absent();
    }

    @Override
    public String toString() {
        return "OrderedMapModificationStrategy [entry=" + entryStrategy + "]";
    }
}