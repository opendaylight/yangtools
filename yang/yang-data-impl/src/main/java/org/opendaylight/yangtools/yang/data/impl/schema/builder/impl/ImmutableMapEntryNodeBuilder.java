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
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.AugmentationIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.api.DataContainerNodeAttrBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImmutableMapEntryNodeBuilder
        extends AbstractImmutableDataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> {
    private static final Logger LOG = LoggerFactory.getLogger(ImmutableMapEntryNodeBuilder.class);
    protected final Map<QName, PathArgument> childrenQNamesToPaths;

    protected ImmutableMapEntryNodeBuilder() {
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

    public static @NonNull DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> create() {
        return new ImmutableMapEntryNodeBuilder();
    }

    public static @NonNull DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> create(
            final int sizeHint) {
        return new ImmutableMapEntryNodeBuilder(sizeHint);
    }

    public static @NonNull DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> create(
            final MapEntryNode node) {
        if (!(node instanceof ImmutableMapEntryNode)) {
            throw new UnsupportedOperationException(String.format("Cannot initialize from class %s", node.getClass()));
        }

        return new ImmutableMapEntryNodeBuilder((ImmutableMapEntryNode)node);
    }

    private static void fillQnames(final Iterable<DataContainerChild<? extends PathArgument, ?>> iterable,
            final Map<QName, PathArgument> out) {
        for (final DataContainerChild<? extends PathArgument, ?> childId : iterable) {
            final PathArgument identifier = childId.getIdentifier();

            // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
            if (isAugment(identifier)) {
                continue;
            }

            out.put(childId.getNodeType(), identifier);
        }
    }


    @Override
    public DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> withValue(
            final Collection<DataContainerChild<? extends PathArgument, ?>> withValue) {
        fillQnames(withValue, childrenQNamesToPaths);
        return super.withValue(withValue);
    }

    private static boolean isAugment(final PathArgument identifier) {
        return identifier instanceof AugmentationIdentifier;
    }

    @Override
    public DataContainerNodeAttrBuilder<NodeIdentifierWithPredicates, MapEntryNode> withChild(
            final DataContainerChild<?, ?> child) {
        // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
        if (!isAugment(child.getIdentifier())) {
            childrenQNamesToPaths.put(child.getNodeType(), child.getIdentifier());
        }

        return super.withChild(child);
    }

    @Override
    public MapEntryNode build() {
        for (final Entry<QName, Object> key : getNodeIdentifier().getKeyValues().entrySet()) {
            final DataContainerChild<?, ?> childNode = getChild(childrenQNamesToPaths.get(key.getKey()));

            // We have enough information to fill-in missing leaf nodes, so let's do that
            if (childNode == null) {
                LeafNode<Object> leaf = ImmutableNodes.leafNode(key.getKey(), key.getValue());
                LOG.debug("Adding leaf {} implied by key {}", leaf, key);
                withChild(leaf);
            } else {
                DataValidationException.checkListKey(getNodeIdentifier(), key.getKey(), key.getValue(),
                    childNode.getValue());
            }
        }

        return new ImmutableMapEntryNode(getNodeIdentifier(), buildValue());
    }

    private static final class ImmutableMapEntryNode
            extends AbstractImmutableDataContainerNode<NodeIdentifierWithPredicates> implements MapEntryNode {

        ImmutableMapEntryNode(final NodeIdentifierWithPredicates nodeIdentifier,
                final Map<PathArgument, DataContainerChild<? extends PathArgument, ?>> children) {
            super(children, nodeIdentifier);
        }
    }
}
