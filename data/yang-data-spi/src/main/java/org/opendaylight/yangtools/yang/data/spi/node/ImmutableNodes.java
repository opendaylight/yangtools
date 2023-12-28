/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifierWithPredicates;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.api.schema.LeafSetEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.MapEntryNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode.BuilderFactory;
import org.opendaylight.yangtools.yang.data.spi.node.impl.ImmutableBuilderFactory;
import org.opendaylight.yangtools.yang.data.spi.node.impl.ImmutableLeafNode;
import org.opendaylight.yangtools.yang.data.spi.node.impl.ImmutableLeafSetEntryNode;

/**
 * Utilities for creating immutable implementations of various {@link NormalizedNode}s.
 */
public final class ImmutableNodes {
    private static final @NonNull ImmutableBuilderFactory BUILDER_FACTORY = new ImmutableBuilderFactory();

    private ImmutableNodes() {
        // Hidden on purpose
    }

    public static @NonNull BuilderFactory builderFactory() {
        return BUILDER_FACTORY;
    }

    /**
     * Construct an immutable {@link LeafNode}.
     *
     * @param <T> Type of leaf node value
     * @param name Name of leaf node
     * @param value Value of leaf node
     * @return Leaf node with supplied name and value
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T> @NonNull LeafNode<T> leafNode(final NodeIdentifier name, final T value) {
        return ImmutableLeafNode.of(name, value);
    }

    /**
     * Construct an immutable {@link LeafNode}.
     *
     * @param <T> Type of leaf node value
     * @param name Name of leaf node
     * @param value Value of leaf node
     * @return Leaf node with supplied name and value
     * @throws NullPointerException if any argument is {@code null}
     */
    public static <T> @NonNull LeafNode<T> leafNode(final QName name, final T value) {
        return leafNode(NodeIdentifier.create(name), value);
    }

    public static @NonNull MapEntryNode mapEntry(final NodeIdentifierWithPredicates name) {
        return BUILDER_FACTORY.newMapEntryBuilder(name.size()).withNodeIdentifier(name).build();
    }

    public static <T> @NonNull LeafSetEntryNode<T> leafSetEntry(final NodeWithValue<T> name) {
        return ImmutableLeafSetEntryNode.of(name);
    }

    public static <T> @NonNull LeafSetEntryNode<T> leafSetEntry(final QName name, final T value) {
        return leafSetEntry(new NodeWithValue<>(name, value));
    }
}
