/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Map;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;

import com.google.common.base.Preconditions;

public class ImmutableAugmentationNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> {

    public static DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> create() {
        return new ImmutableAugmentationNodeBuilder();
    }

    @Override
    public DataContainerNodeBuilder<InstanceIdentifier.AugmentationIdentifier, AugmentationNode> withChild(
            final DataContainerChild<?, ?> child) {
        // Check nested augments
        DataValidationException.checkLegalData(child instanceof AugmentationNode == false,
                "Unable to add: %s, as a child for: %s, Nested augmentations are not permitted", child.getNodeType(),
                getNodeIdentifier() == null ? this : getNodeIdentifier());

        return super.withChild(child);
    }

    @Override
    public AugmentationNode build() {
        return new ImmutableAugmentationNode(getNodeIdentifier(), buildValue());
    }

    static final class ImmutableAugmentationNode
            extends AbstractImmutableDataContainerNode<InstanceIdentifier.AugmentationIdentifier>
            implements AugmentationNode {

        ImmutableAugmentationNode(final InstanceIdentifier.AugmentationIdentifier nodeIdentifier, final Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children) {
            super(children, nodeIdentifier);
        }
    }
}
