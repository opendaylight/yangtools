/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.tree;

import static com.google.common.base.Preconditions.checkArgument;

import com.google.common.base.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.tree.TreeType;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeContainerBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.ImmutableLeafSetNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;

final class UnorderedLeafSetModificationStrategy extends AbstractNodeContainerModificationStrategy {
    private final Optional<ModificationApplyOperation> entryStrategy;

    @SuppressWarnings({ "unchecked", "rawtypes" })
    UnorderedLeafSetModificationStrategy(final LeafListSchemaNode schema, final TreeType treeType) {
        super((Class) LeafSetNode.class, treeType);
        entryStrategy = Optional.<ModificationApplyOperation> of(new LeafSetEntryModificationStrategy(schema));
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected NormalizedNodeContainerBuilder createBuilder(final NormalizedNode<?, ?> original) {
        checkArgument(original instanceof LeafSetNode<?>);
        return ImmutableLeafSetNodeBuilder.create((LeafSetNode<?>) original);
    }

    @Override
    public Optional<ModificationApplyOperation> getChild(final YangInstanceIdentifier.PathArgument identifier) {
        if (identifier instanceof YangInstanceIdentifier.NodeWithValue) {
            return entryStrategy;
        }
        return Optional.absent();
    }
}