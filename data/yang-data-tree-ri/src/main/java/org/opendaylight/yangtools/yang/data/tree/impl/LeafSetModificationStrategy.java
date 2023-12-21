/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.SystemLeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.UserLeafSetNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableUserLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.data.tree.api.DataTreeConfiguration;
import org.opendaylight.yangtools.yang.data.tree.impl.AbstractNodeContainerModificationStrategy.Invisible;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class LeafSetModificationStrategy extends Invisible<LeafListSchemaNode> {
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final NormalizedNodeContainerSupport<NodeIdentifier, UserLeafSetNode<?>> ORDERED_SUPPORT =
            new NormalizedNodeContainerSupport(UserLeafSetNode.class, ChildTrackingPolicy.ORDERED,
                foo -> ImmutableUserLeafSetNodeBuilder.create((UserLeafSetNode<?>) foo),
                ImmutableUserLeafSetNodeBuilder::new);
    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static final NormalizedNodeContainerSupport<NodeIdentifier, SystemLeafSetNode<?>> UNORDERED_SUPPORT =
            new NormalizedNodeContainerSupport(SystemLeafSetNode.class,
                foo -> ImmutableLeafSetNodeBuilder.create((SystemLeafSetNode<?>) foo),
                ImmutableLeafSetNodeBuilder::new);

    LeafSetModificationStrategy(final LeafListSchemaNode schema, final DataTreeConfiguration treeConfig) {
        super(schema.isUserOrdered() ? ORDERED_SUPPORT : UNORDERED_SUPPORT, treeConfig,
                new ValueNodeModificationStrategy<>(LeafSetEntryNode.class, schema));
    }

    @Override
    public ModificationApplyOperation childByArg(final PathArgument arg) {
        return arg instanceof NodeWithValue ? entryStrategy() : null;
    }
}