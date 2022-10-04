/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;

public final class ImmutableAugmentationNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> {

    ImmutableAugmentationNodeBuilder() {
    }

    ImmutableAugmentationNodeBuilder(final int sizeHint) {
        super(sizeHint);
    }

    ImmutableAugmentationNodeBuilder(final ImmutableAugmentationNode node) {
        super(node);
    }

    public static @NonNull DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> create() {
        return new ImmutableAugmentationNodeBuilder();
    }

    public static @NonNull DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> create(
            final int sizeHint) {
        return new ImmutableAugmentationNodeBuilder(sizeHint);
    }

    public static @NonNull DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> create(
            final AugmentationNode node) {
        if (!(node instanceof ImmutableAugmentationNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableAugmentationNodeBuilder((ImmutableAugmentationNode)node);
    }

    @Override
    public DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> withChild(
            final DataContainerChild child) {
        // Check nested augments
        if (child instanceof AugmentationNode) {
            final AugmentationIdentifier myId = getNodeIdentifier();
            throw new DataValidationException(String.format(
                "Unable to add: %s, as a child for: %s, Nested augmentations are not permitted", child.getIdentifier(),
                myId == null ? this : myId));
        }

        return super.withChild(child);
    }

    @Override
    public DataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> withoutChild(final PathArgument key) {
        return super.withoutChild(key);
    }

    @Override
    public AugmentationNode build() {
        return new ImmutableAugmentationNode(getNodeIdentifier(), buildValue());
    }

    private static final class ImmutableAugmentationNode
            extends AbstractImmutableDataContainerNode<AugmentationIdentifier, AugmentationNode>
            implements AugmentationNode {

        ImmutableAugmentationNode(final AugmentationIdentifier nodeIdentifier,
                final Map<PathArgument, Object> children) {
            super(children, nodeIdentifier);
        }

        @Override
        protected Class<AugmentationNode> implementedType() {
            return AugmentationNode.class;
        }
    }
}
