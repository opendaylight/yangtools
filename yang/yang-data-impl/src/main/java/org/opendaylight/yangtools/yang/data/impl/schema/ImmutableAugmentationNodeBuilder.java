/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.Map;

import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ImmutableAugmentationNodeBuilder {
    private InstanceIdentifier.AugmentationIdentifier nodeIdentifier;
    private final Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children;

    // TODO augmentationNode builder has no purpose without schema ?

    // TODO same as ContainerBuilder, different Node type

    protected ImmutableAugmentationNodeBuilder() {
        this.children = Maps.newHashMap();
    }

    public static ImmutableAugmentationNodeBuilder get() {
        return new ImmutableAugmentationNodeBuilder();
    }

    // TODO Node Identifier might be passed to constructor,
    public ImmutableAugmentationNodeBuilder withNodeIdentifier(InstanceIdentifier.AugmentationIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    public ImmutableAugmentationNodeBuilder withChildren(Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children) {
        for (DataContainerChild<?, ?> child : children.values()) {
            withChild(child);
        }
        return this;
    }

    public ImmutableAugmentationNodeBuilder withChild(DataContainerChild<?, ?> child) {
        Preconditions.checkArgument(child instanceof AugmentationNode == false,
                "Unable to add: %s, as a child for: %s, Nested augmentations are not permitted", child.getNodeType(),
                nodeIdentifier == null ? this : nodeIdentifier);

        this.children.put(child.getIdentifier(), child);
        return this;
    }

    public AugmentationNode build() {
        return new ImmutableAugmentationNode(nodeIdentifier, children);
    }
}
