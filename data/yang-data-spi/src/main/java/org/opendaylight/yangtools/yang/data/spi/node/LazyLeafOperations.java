/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.VerifyException;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.spi.node.impl.ImmutableLeafNode;

/**
 * Support utilities for dealing with Maps which would normally hold {@link DataContainerChild} values, but are modified
 * to eliminate {@link LeafNode} instances.
 */
@Beta
public final class LazyLeafOperations {
    private LazyLeafOperations() {
        // Hidden on purpose
    }

    public static @Nullable DataContainerChild getChild(final Map<NodeIdentifier, Object> map,
            final NodeIdentifier key) {
        final var value = map.get(key);
        return value == null ? null : decodeChild(key, value);
    }

    public static void putChild(final Map<NodeIdentifier, Object> map, final DataContainerChild child) {
        final var node = requireNonNull(child);
        map.put(node.name(), encodeExpendableChild(node));
    }

    static @NonNull LeafNode<?> coerceLeaf(final NodeIdentifier key, final Object value) {
        return ImmutableLeafNode.of(key, value);
    }

    private static @Nullable DataContainerChild decodeChild(final NodeIdentifier key, final @NonNull Object value) {
        return decodeExpendableChild(key, value);
    }

    private static @NonNull DataContainerChild decodeExpendableChild(final NodeIdentifier key,
            final @NonNull Object value) {
        return value instanceof DataContainerChild child ? child : coerceLeaf(key, value);
    }

    private static @NonNull Object encodeExpendableChild(final @NonNull DataContainerChild node) {
        return node instanceof LeafNode<?> leafNode ? verifyEncode(leafNode.body()) : node;
    }

    private static @NonNull Object verifyEncode(final @NonNull Object value) {
        if (value instanceof DataContainerChild) {
            throw new VerifyException("Unexpected leaf value " + value);
        }
        return value;
    }
}
