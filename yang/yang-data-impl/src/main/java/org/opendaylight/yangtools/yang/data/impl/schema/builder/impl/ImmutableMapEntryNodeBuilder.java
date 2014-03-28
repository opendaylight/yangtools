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
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerAttrNode;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class ImmutableMapEntryNodeBuilder
        extends AbstractImmutableDataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> {

    protected final Map<QName, InstanceIdentifier.PathArgument> childrenQNamesToPaths;

    protected ImmutableMapEntryNodeBuilder() {
        this.childrenQNamesToPaths = Maps.newLinkedHashMap();
    }

    public static DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> create() {
        return new ImmutableMapEntryNodeBuilder();
    }

    @Override
    public DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> withValue(final List<DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> value) {
        for (final DataContainerChild<? extends InstanceIdentifier.PathArgument, ?> childId : value) {
            final InstanceIdentifier.PathArgument identifier = childId.getIdentifier();

            // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
            if(isAugment(identifier)) {
                continue;
            }

            this.childrenQNamesToPaths.put(childId.getNodeType(), identifier);
        }
        return super.withValue(value);
    }

    private static boolean isAugment(InstanceIdentifier.PathArgument identifier) {
        return identifier instanceof InstanceIdentifier.AugmentationIdentifier;
    }

    @Override
    public DataContainerNodeAttrBuilder<InstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> withChild(final DataContainerChild<?, ?> child) {
        // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
        if(isAugment(child.getIdentifier()) == false) {
            childrenQNamesToPaths.put(child.getNodeType(), child.getIdentifier());
        }

        return super.withChild(child);
    }

    @Override
    public MapEntryNode build() {
        checkKeys();
        return new ImmutableMapEntryNode(getNodeIdentifier(), buildValue(), attributes);
    }

    private void checkKeys() {
        for (final QName keyQName : getNodeIdentifier().getKeyValues().keySet()) {
            DataContainerChild<?, ?> childNode = getChild(childrenQNamesToPaths.get(keyQName));
            DataValidationException.checkListKey(childNode, getNodeIdentifier().getKeyValues(), keyQName, getNodeIdentifier());
        }
    }

    static final class ImmutableMapEntryNode extends AbstractImmutableDataContainerAttrNode<InstanceIdentifier.NodeIdentifierWithPredicates> implements MapEntryNode {

        ImmutableMapEntryNode(final InstanceIdentifier.NodeIdentifierWithPredicates nodeIdentifier,
                              final Map<InstanceIdentifier.PathArgument, DataContainerChild<? extends InstanceIdentifier.PathArgument, ?>> children, final Map<QName, String> attributes) {
            super(children, nodeIdentifier, attributes);
        }
    }
}
