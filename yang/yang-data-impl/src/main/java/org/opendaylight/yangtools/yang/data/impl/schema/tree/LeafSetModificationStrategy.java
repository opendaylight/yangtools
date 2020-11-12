/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.OrderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UnorderedLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableOrderedLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.tree.AbstractNodeContainerModificationStrategy.Invisible;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

@NonNullByDefault
final class LeafSetModificationStrategy extends Invisible<LeafListSchemaNode> {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final NormalizedNodeContainerSupport<NodeIdentifier, OrderedLeafSetNode<?>> ORDERED_SUPPORT =
            new NormalizedNodeContainerSupport(OrderedLeafSetNode.class, ChildTrackingPolicy.ORDERED,
                foo -> ImmutableOrderedLeafSetNodeBuilder.create((OrderedLeafSetNode<?>)foo),
                ImmutableOrderedLeafSetNodeBuilder::create);
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final NormalizedNodeContainerSupport<NodeIdentifier, UnorderedLeafSetNode<?>> UNORDERED_SUPPORT =
            new NormalizedNodeContainerSupport(UnorderedLeafSetNode.class,
                foo -> ImmutableLeafSetNodeBuilder.create((UnorderedLeafSetNode<?>)foo),
                ImmutableLeafSetNodeBuilder::create);

    LeafSetModificationStrategy(final LeafListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(schema.isUserOrdered() ? ORDERED_SUPPORT : UNORDERED_SUPPORT, treeConfig,
                new ValueNodeModificationStrategy<>(LeafSetEntryNode.class, schema));
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final PathArgument identifier) {
        return identifier instanceof NodeWithValue ? entryStrategy() : Optional.empty();
    }
}