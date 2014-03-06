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
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

import com.google.common.collect.Maps;

public class ImmutableContainerNodeBuilder {
    private InstanceIdentifier.NodeIdentifier nodeIdentifier;
    private final Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children;

    protected ImmutableContainerNodeBuilder() {
        this.children = Maps.newHashMap();
    }

    public static ImmutableContainerNodeBuilder get() {
        return new ImmutableContainerNodeBuilder();
    }

    public ImmutableContainerNodeBuilder withNodeIdentifier(InstanceIdentifier.NodeIdentifier nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    public ImmutableContainerNodeBuilder withChildren(Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children) {
        for (DataContainerChild<?, ?> child : children.values()) {
            withChild(child);
        }
        return this;
    }

    public ImmutableContainerNodeBuilder withChild(DataContainerChild<?, ?> child) {
        this.children.put(child.getIdentifier(), child);
        return this;
    }

    public ImmutableContainerNode build() {
        return new ImmutableContainerNode(nodeIdentifier, children);
    }
}
