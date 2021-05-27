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
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.AbstractImmutableDataContainerNode;
import org.opendaylight.yangtools.yang.model.api.ListSchemaNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImmutableMapEntryNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> {
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
        fillQNames(node.body(), childrenQNamesToPaths);
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> create() {
        return new ImmutableMapEntryNodeBuilder();
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> create(
            final int sizeHint) {
        return new ImmutableMapEntryNodeBuilder(sizeHint);
    }

    public static @NonNull DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> create(
            final MapEntryNode node) {
        if (!(node instanceof ImmutableMapEntryNode)) {
            throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
        }

        return new ImmutableMapEntryNodeBuilder((ImmutableMapEntryNode)node);
    }

    @Deprecated(since = "6.0.7", forRemoval = true)
    public static @NonNull DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> create(
            final ListSchemaNode schema) {
        return new SchemaAwareImmutableMapEntryNodeBuilder(schema);
    }

    private static void fillQNames(final Iterable<DataContainerChild> iterable, final Map<QName, PathArgument> out) {
        for (final DataContainerChild child : iterable) {
            putQName(out, child);
        }
    }

    private static void putQName(final Map<QName, PathArgument> map, final DataContainerChild child) {
        // Augmentation nodes cannot be keys, and do not have to be present in childrenQNamesToPaths map
        final PathArgument identifier = child.getIdentifier();
        if (!(identifier instanceof AugmentationIdentifier)) {
            map.put(identifier.getNodeType(), identifier);
        }
    }

    @Override
    public DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> withValue(
            final Collection<DataContainerChild> withValue) {
        fillQNames(withValue, childrenQNamesToPaths);
        return super.withValue(withValue);
    }

    @Override
    public DataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode> withChild(
            final DataContainerChild child) {
        putQName(childrenQNamesToPaths, child);
        return super.withChild(child);
    }

    @Override
    public MapEntryNode build() {
        for (final Entry<QName, Object> key : getNodeIdentifier().entrySet()) {
            final DataContainerChild childNode = getChild(childrenQNamesToPaths.get(key.getKey()));

            // We have enough information to fill-in missing leaf nodes, so let's do that
            if (childNode == null) {
                LeafNode<Object> leaf = ImmutableNodes.leafNode(key.getKey(), key.getValue());
                LOG.debug("Adding leaf {} implied by key {}", leaf, key);
                withChild(leaf);
            } else {
                DataValidationException.checkListKey(getNodeIdentifier(), key.getKey(), key.getValue(),
                    childNode.body());
            }
        }

        return new ImmutableMapEntryNode(getNodeIdentifier(), buildValue());
    }

    private static final class ImmutableMapEntryNode
            extends AbstractImmutableDataContainerNode<NodeIdentifierWithPredicates, MapEntryNode>
            implements MapEntryNode {

        ImmutableMapEntryNode(final NodeIdentifierWithPredicates nodeIdentifier,
                final Map<PathArgument, Object> children) {
            super(children, nodeIdentifier);
        }

        @Override
        protected Class<MapEntryNode> implementedType() {
            return MapEntryNode.class;
        }
    }
}
