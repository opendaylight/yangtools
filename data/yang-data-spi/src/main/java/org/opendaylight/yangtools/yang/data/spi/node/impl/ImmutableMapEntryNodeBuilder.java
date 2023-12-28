/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.util.ImmutableOffsetMap;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractMapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.builder.DataContainerNodeBuilder;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;
import org.opendaylight.yangtools.yang.data.impl.schema.builder.impl.valid.DataValidationException;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.LazyLeafOperations;
import org.opendaylight.yangtools.yang.data.impl.schema.nodes.LazyValues;
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
        super(node.name, node.children);
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
        for (var key : getNodeIdentifier().entrySet()) {
            final var childNode = getChild(childrenQNamesToPaths.get(key.getKey()));

            // We have enough information to fill-in missing leaf nodes, so let's do that
            if (childNode == null) {
                final var leaf = ImmutableNodes.leafNode(key.getKey(), key.getValue());
                LOG.debug("Adding leaf {} implied by key {}", leaf, key);
                withChild(leaf);
            } else {
                DataValidationException.checkListKey(getNodeIdentifier(), key.getKey(), key.getValue(),
                    childNode.body());
            }
        }

        return new ImmutableMapEntryNode(getNodeIdentifier(), buildValue());
    }

    private static final class ImmutableMapEntryNode extends AbstractMapEntryNode {
        private final @NonNull NodeIdentifierWithPredicates name;
        private final @NonNull Map<NodeIdentifier, Object> children;

        ImmutableMapEntryNode(final NodeIdentifierWithPredicates name, final Map<NodeIdentifier, Object> children) {
            this.name = requireNonNull(name);
            // FIXME: move this to caller
            this.children = ImmutableOffsetMap.unorderedCopyOf(children);
        }

        @Override
        public NodeIdentifierWithPredicates name() {
            return name;
        }

        @Override
        public DataContainerChild childByArg(final NodeIdentifier child) {
            return LazyLeafOperations.getChild(children, child);
        }

        @Override
        public Collection<DataContainerChild> body() {
            return new LazyValues(children);
        }

        @Override
        public int size() {
            return children.size();
        }

        @Override
        protected int valueHashCode() {
            return children.hashCode();
        }

        @Override
        protected boolean valueEquals(final MapEntryNode other) {
            return other instanceof ImmutableMapEntryNode immutable ? children.equals(immutable.children)
                : ImmutableNormalizedNodeMethods.bodyEquals(this, other);
        }
    }
}
