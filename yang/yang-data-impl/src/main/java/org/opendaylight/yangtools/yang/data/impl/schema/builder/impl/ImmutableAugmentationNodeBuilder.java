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
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;

public class ImmutableAugmentationNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<AugmentationIdentifier, AugmentationNode> {

    protected ImmutableAugmentationNodeBuilder() {
    }

    protected ImmutableAugmentationNodeBuilder(final int sizeHint) {
        super(sizeHint);
    }

    public ImmutableAugmentationNodeBuilder(final ImmutableAugmentationNode node) {
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
            final DataContainerChild<?, ?> child) {
        // Check nested augments
        DataValidationException.checkLegalData(!(child instanceof AugmentationNode),
                "Unable to add: %s, as a child for: %s, Nested augmentations are not permitted", child.getNodeType(),
                getNodeIdentifier() == null ? this : getNodeIdentifier());

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
            extends AbstractImmutableDataContainerNode<AugmentationIdentifier> implements AugmentationNode {

        ImmutableAugmentationNode(final AugmentationIdentifier nodeIdentifier,
                final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children) {
            super(children, nodeIdentifier);
        }
    }
}
