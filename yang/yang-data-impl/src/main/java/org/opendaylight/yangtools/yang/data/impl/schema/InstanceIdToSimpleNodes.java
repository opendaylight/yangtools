/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Iterator;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

/**
* Base strategy for converting an instance identifier into a normalized node structure for leaf and leaf-list types.
*/
abstract class InstanceIdToSimpleNodes<T extends PathArgument> extends InstanceIdToNodes<T> {

    InstanceIdToSimpleNodes(final T identifier) {
        super(identifier);
    }

    @Override
    final NormalizedNode<?, ?> create(final PathArgument first, final Iterator<PathArgument> others,
            final Optional<NormalizedNode<?, ?>> deepestChild) {
        final NormalizedNodeBuilder<? extends PathArgument, Object,
                ? extends NormalizedNode<? extends PathArgument, Object>> builder = getBuilder(first);

        if (deepestChild.isPresent()) {
            builder.withValue(deepestChild.get().getValue());
        }

        return builder.build();
    }

    @Override
    final InstanceIdToNodes<?> getChild(final PathArgument child) {
        return null;
    }

    @Override
    final boolean isMixin() {
        return false;
    }

    abstract NormalizedNodeBuilder<? extends PathArgument, Object,
            ? extends NormalizedNode<? extends PathArgument, Object>> getBuilder(PathArgument node);

    static final class LeafNormalization extends InstanceIdToSimpleNodes<NodeIdentifier> {
        LeafNormalization(final LeafSchemaNode potential) {
            super(new NodeIdentifier(potential.getQName()));
        }

        @Override
        NormalizedNodeBuilder<NodeIdentifier, Object, LeafNode<Object>> getBuilder(final PathArgument node) {
            return Builders.leafBuilder().withNodeIdentifier(getIdentifier());
        }
    }

    static final class LeafListEntryNormalization extends InstanceIdToSimpleNodes<NodeWithValue> {
        LeafListEntryNormalization(final LeafListSchemaNode potential) {
            super(new NodeWithValue<>(potential.getQName(), null));
        }

        @Override
        NormalizedNodeBuilder<NodeWithValue, Object, LeafSetEntryNode<Object>> getBuilder(final PathArgument node) {
            checkArgument(node instanceof NodeWithValue);
            return Builders.leafSetEntryBuilder().withNodeIdentifier((NodeWithValue<?>) node)
                    .withValue(((NodeWithValue<?>) node).getValue());
        }
    }
}
