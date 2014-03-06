/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema;

import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ImmutableMapEntryNodeBuilder {
    private InstanceIdentifier.NodeIdentifierWithPredicates nodeIdentifier;
    protected final Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children;
    protected final Map<QName, InstanceIdentifier.PathArgument> childrenQNamesToPaths;

    protected ImmutableMapEntryNodeBuilder() {
        this.children = Maps.newHashMap();
        this.childrenQNamesToPaths = Maps.newHashMap();
    }

    public static ImmutableMapEntryNodeBuilder get() {
        return new ImmutableMapEntryNodeBuilder();
    }

    public ImmutableMapEntryNodeBuilder withNodeIdentifier(InstanceIdentifier.NodeIdentifierWithPredicates nodeIdentifier) {
        this.nodeIdentifier = nodeIdentifier;
        return this;
    }

    public ImmutableMapEntryNodeBuilder withChildren(Map<InstanceIdentifier.PathArgument, DataContainerChild<?, ?>> children) {
        for (DataContainerChild<?, ?> child : children.values()) {
            withChild(child);
        }
        return this;
    }

    public ImmutableMapEntryNodeBuilder withChild(DataContainerChild<?, ?> child) {
        this.children.put(child.getIdentifier(), child);
        this.childrenQNamesToPaths.put(child.getNodeType(), child.getIdentifier());
        return this;
    }

    public ImmutableMapEntryNode build() {
        checkKeys();
        return new ImmutableMapEntryNode(nodeIdentifier, children);
    }

    private void checkKeys() {
        for (QName keyQName : nodeIdentifier.getKeyValues().keySet()) {

            // FIXME, find better solution than 2 maps (map from QName to Child ?)
            InstanceIdentifier.PathArgument childNodePath = childrenQNamesToPaths.get(keyQName);
            DataContainerChild<?, ?> childNode = children.get(childNodePath);

            Preconditions.checkNotNull(childNode, "Key child node: %s, not present", keyQName);

            Object actualValue = nodeIdentifier.getKeyValues().get(keyQName);
            Object expectedValue = childNode.getValue();
            Preconditions.checkArgument(expectedValue.equals(actualValue),
                    "Key child node with unexpected value, is: %s, should be: %s", actualValue, expectedValue);
        }
    }
}
