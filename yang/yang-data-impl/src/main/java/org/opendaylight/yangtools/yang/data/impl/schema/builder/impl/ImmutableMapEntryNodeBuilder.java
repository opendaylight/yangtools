/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.List;
import java.util.Map;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.InstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AugmentationNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class ImmutableMapEntryNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> {

    protected final Map<QName, InstanceIdentifier.PathArgument> childrenQNamesToPaths;

    protected ImmutableMapEntryNodeBuilder() {
        this.childrenQNamesToPaths = Maps.newLinkedHashMap();
    }

    public static DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> create() {
        return new ImmutableMapEntryNodeBuilder();
    }

    // FIXME, find better solution than 2 maps (map from QName to Child ?)

    @Override
    public DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> withValue(final List<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> value) {
        for (DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> childId : value) {
            InstanceIdentifier.PathArgument identifier = childId.getIdentifier();

            // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
            if(identifier instanceof InstanceIdentifier.AugmentationIdentifier) {
                continue;
            }

            this.childrenQNamesToPaths.put(childId.getNodeType(), identifier);
        }
        return super.withValue(value);
    }

    @Override
    public DataContainerNodeBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> withChild(final DataContainerChild<?, ?> child) {
        // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
        if(child.getIdentifier() instanceof InstanceIdentifier.AugmentationIdentifier == false) {
            childrenQNamesToPaths.put(child.getNodeType(), child.getIdentifier());
        }
        return super.withChild(child);
    }

    @Override
    public MapEntryNode build() {
        checkKeys();
        return new ImmutableMapEntryNode(nodeIdentifier, value);
    }

    private void checkKeys() {
        for (QName keyQName : nodeIdentifier.getKeyValues().keySet()) {

            InstanceIdentifier.PathArgument childNodePath = childrenQNamesToPaths.get(keyQName);
            DataContainerChild<?, ?> childNode = value.get(childNodePath);

            Preconditions.checkNotNull(childNode, "Key child node: %s, not present", keyQName);

            Object actualValue = nodeIdentifier.getKeyValues().get(keyQName);
            Object expectedValue = childNode.getValue();
            Preconditions.checkArgument(expectedValue.equals(actualValue),
                    "Key child node with unexpected value, is: %s, should be: %s", actualValue, expectedValue);
        }
    }

    static final class ImmutableMapEntryNode extends AbstractImmutableDataContainerNode<InstanceIdentifier.NodeIdentifierWithPredicates> implements MapEntryNode {

        ImmutableMapEntryNode(final InstanceIdentifier.NodeIdentifierWithPredicates nodeIdentifier,
                              final Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children) {
            super(ImmutableMap.copyOf(children), nodeIdentifier);
        }
    }
}
