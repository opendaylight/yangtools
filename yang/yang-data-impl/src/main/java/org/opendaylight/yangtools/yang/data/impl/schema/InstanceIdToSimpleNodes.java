/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.ModifyAction;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.NormalizedNodeAttrBuilder;
import org.opendaylight.yangtools.yang.model.api.LeafListSchemaNode;
import org.opendaylight.yangtools.yang.model.api.LeafSchemaNode;

/**
* Base strategy for converting an instance identifier into a normalized node structure for leaf and leaf-list types.
*/
abstract class InstanceIdToSimpleNodes<T extends YangInstanceIdentifier.PathArgument> extends InstanceIdToNodes<T> {

    protected InstanceIdToSimpleNodes(final T identifier) {
        super(identifier);
    }

    @Override
    public NormalizedNode<?, ?> create(final YangInstanceIdentifier instanceId, final Optional<NormalizedNode<?, ?>> deepestChild, final Optional<Map.Entry<QName,ModifyAction>> operation) {
        checkNotNull(instanceId);
        final YangInstanceIdentifier.PathArgument pathArgument = instanceId.getPathArguments().get(0);
        final NormalizedNodeAttrBuilder<? extends YangInstanceIdentifier.PathArgument, Object, ? extends NormalizedNode<? extends YangInstanceIdentifier.PathArgument, Object>> builder = getBuilder(pathArgument);

        if(deepestChild.isPresent()) {
            builder.withValue(deepestChild.get().getValue());
        }

        addModifyOpIfPresent(operation, builder);
        return builder.build();
    }

    protected abstract NormalizedNodeAttrBuilder<? extends YangInstanceIdentifier.PathArgument, Object, ? extends NormalizedNode<? extends YangInstanceIdentifier.PathArgument, Object>> getBuilder(YangInstanceIdentifier.PathArgument node);

    @Override
    public InstanceIdToNodes<?> getChild(final YangInstanceIdentifier.PathArgument child) {
        return null;
    }

    static final class LeafNormalization extends InstanceIdToSimpleNodes<YangInstanceIdentifier.NodeIdentifier> {

        protected LeafNormalization(final LeafSchemaNode potential) {
            super(new YangInstanceIdentifier.NodeIdentifier(potential.getQName()));
        }

        @Override
        protected NormalizedNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifier, Object, LeafNode<Object>> getBuilder(final YangInstanceIdentifier.PathArgument node) {
            return Builders.leafBuilder().withNodeIdentifier(getIdentifier());
        }
    }

    static final class LeafListEntryNormalization extends InstanceIdToSimpleNodes<YangInstanceIdentifier.NodeWithValue> {

        public LeafListEntryNormalization(final LeafListSchemaNode potential) {
            super(new YangInstanceIdentifier.NodeWithValue(potential.getQName(), null));
        }

        @Override
        protected NormalizedNodeAttrBuilder<YangInstanceIdentifier.NodeWithValue, Object, LeafSetEntryNode<Object>> getBuilder(final YangInstanceIdentifier.PathArgument node) {
            Preconditions.checkArgument(node instanceof YangInstanceIdentifier.NodeWithValue);
            return Builders.leafSetEntryBuilder().withNodeIdentifier((YangInstanceIdentifier.NodeWithValue) node).withValue(((YangInstanceIdentifier.NodeWithValue) node).getValue());
        }

    }
}
