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
import org.opendaylight.yangtools.yang.data.api.schema.ChoiceNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

import com.google.common.collect.Maps;

public class ImmutableChoiceNodeBuilder {
    private InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private final Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children;

    // TODO augmentationNode builder has no purpose without schema ?

    // TODO same as ContainerBuilder

    protected ImmutableChoiceNodeBuilder() {
        this.children = Maps.newHashMap();
    }

    public static ImmutableChoiceNodeBuilder get() {
        return new ImmutableChoiceNodeBuilder();
    }

    public ImmutableChoiceNodeBuilder withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    public ImmutableChoiceNodeBuilder withChildren(Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children) {
        for (DataContainerChild<?, ?> child : children.values()) {
            withChild(child);
        }
        return this;
    }

    public ImmutableChoiceNodeBuilder withChild(DataContainerChild<?, ?> child) {
        this.children.put(child.getIdentifier(), child);
        return this;
    }

    public ChoiceNode build() {
        return new ImmutableChoiceNode(nodeIdentifier, children);
    }
}
