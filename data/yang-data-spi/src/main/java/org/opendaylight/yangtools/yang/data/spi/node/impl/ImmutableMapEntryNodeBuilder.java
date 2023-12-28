/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ImmutableMapEntryNodeBuilder
        extends AbstractImmutableDataContainerNodeBuilder<NodeIdentifierWithPredicates, MapEntryNode>
        implements MapEntryNode.Builder {
    private static final Logger LOG = LoggerFactory.getLogger(ImmutableMapEntryNodeBuilder.class);

    private final Map<QName, NodeIdentifier> childrenQNamesToPaths;

    public ImmutableMapEntryNodeBuilder() {
        childrenQNamesToPaths = new LinkedHashMap<>();
    }

    public ImmutableMapEntryNodeBuilder(final int sizeHint) {
        super(sizeHint);
        childrenQNamesToPaths = new LinkedHashMap<>(sizeHint);
    }

    private ImmutableMapEntryNodeBuilder(final ImmutableMapEntryNode node) {
        super(node.name(), node.children);
        childrenQNamesToPaths = new LinkedHashMap<>();
        fillQNames(node.body(), childrenQNamesToPaths);
    }

    public static MapEntryNode.@NonNull Builder create(final MapEntryNode node) {
        if (node instanceof ImmutableMapEntryNode immutableNode) {
            return new ImmutableMapEntryNodeBuilder(immutableNode);
        }
        throw new UnsupportedOperationException("Cannot initialize from class " + node.getClass());
    }

    private static void fillQNames(final Iterable<DataContainerChild> iterable, final Map<QName, NodeIdentifier> out) {
        for (var child : iterable) {
            putQName(out, child);
        }
    }

    private static void putQName(final Map<QName, NodeIdentifier> map, final DataContainerChild child) {
        final var identifier = child.name();
        map.put(identifier.getNodeType(), identifier);
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
        final var name = getNodeIdentifier();

        for (var key : name.entrySet()) {
            final var childNode = getChild(childrenQNamesToPaths.get(key.getKey()));

            // We have enough information to fill-in missing leaf nodes, so let's do that
            if (childNode == null) {
                final var leaf = ImmutableLeafNode.of(new NodeIdentifier(key.getKey()), key.getValue());
                LOG.debug("Adding leaf {} implied by key {}", leaf, key);
                withChild(leaf);
            } else {
                final var keyQName = key.getKey();
                final var expected = key.getValue();
                final var actual = childNode.body();
                // Objects.equals() does not deal with arrays, but is faster
                if (!Objects.equals(expected, actual) && !Objects.deepEquals(expected, actual)) {
                    throw new IllegalStateException(
                        "Illegal value for key: %s, in: %s, actual value: %s, expected value from key: %s".formatted(
                        keyQName, name, actual, expected));
                }
            }
        }

        return new ImmutableMapEntryNode(getNodeIdentifier(), buildValue());
    }
}
