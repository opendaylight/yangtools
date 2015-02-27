/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.builder.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerAttrNode;

public class ImmutableMapEntryNodeBuilder extends AbstractImmutableDataContainerNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> {

    protected final Map<QName, YangInstanceIdentifier.PathArgument> childrenQNamesToPaths;

    protected ImmutableMapEntryNodeBuilder() {
        super();
        this.childrenQNamesToPaths = new LinkedHashMap<>();
    }

    protected ImmutableMapEntryNodeBuilder(final int sizeHint) {
        super(sizeHint);
        this.childrenQNamesToPaths = new LinkedHashMap<>(sizeHint);
    }

    protected ImmutableMapEntryNodeBuilder(final ImmutableMapEntryNode node) {
        super(node);
        this.childrenQNamesToPaths = new LinkedHashMap<>();
        fillQnames(node.getValue(), childrenQNamesToPaths);
    }

    public static DataContainerNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> create() {
        return new ImmutableMapEntryNodeBuilder();
    }

    public static DataContainerNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> create(final int sizeHint) {
        return new ImmutableMapEntryNodeBuilder(sizeHint);
    }

    public static DataContainerNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> create(final MapEntryNode node) {
        if (!(node instanceof ImmutableMapEntryNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableMapEntryNodeBuilder((ImmutableMapEntryNode)node);
    }

    private static void fillQnames(final Iterable<DataContainerChild<? extends PathArgument, ?>> iterable, final Map<QName, PathArgument> out) {
        for (final DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?> childId : iterable) {
            final YangInstanceIdentifier.PathArgument identifier = childId.getIdentifier();

            // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
            if(isAugment(identifier)) {
                continue;
            }

            out.put(childId.getNodeType(), identifier);
        }
    }


    @Override
    public DataContainerNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> withValue(final Collection<DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?>> value) {
        fillQnames(value, childrenQNamesToPaths);
        return super.withValue(value);
    }

    private static boolean isAugment(final YangInstanceIdentifier.PathArgument identifier) {
        return identifier instanceof YangInstanceIdentifier.AugmentationIdentifier;
    }

    @Override
    public DataContainerNodeAttrBuilder<YangInstanceIdentifier.NodeIdentifierWithPredicates, MapEntryNode> withChild(final DataContainerChild<?, ?> child) {
        // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
        if(!isAugment(child.getIdentifier())) {
            childrenQNamesToPaths.put(child.getNodeType(), child.getIdentifier());
        }

        return super.withChild(child);
    }

    @Override
    public MapEntryNode build() {
        checkKeys();
        return new ImmutableMapEntryNode(getNodeIdentifier(), buildValue(), getAttributes());
    }

    private void checkKeys() {
        for (final QName keyQName : getNodeIdentifier().getKeyValues().keySet()) {
            DataContainerChild<?, ?> childNode = getChild(childrenQNamesToPaths.get(keyQName));
            DataValidationException.checkListKey(childNode, getNodeIdentifier().getKeyValues(), keyQName, getNodeIdentifier());
        }
    }

    private static final class ImmutableMapEntryNode extends AbstractImmutableDataContainerAttrNode<YangInstanceIdentifier.NodeIdentifierWithPredicates> implements MapEntryNode {

        ImmutableMapEntryNode(final YangInstanceIdentifier.NodeIdentifierWithPredicates nodeIdentifier,
                final Map<YangInstanceIdentifier.PathArgument, DataContainerChild<? extends YangInstanceIdentifier.PathArgument, ?>> children, final Map<QName, String> attributes) {
            super(children, nodeIdentifier, attributes);
        }
    }
}
