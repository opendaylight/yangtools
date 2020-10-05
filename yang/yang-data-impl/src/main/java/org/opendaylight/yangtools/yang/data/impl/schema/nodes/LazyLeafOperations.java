/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.schema.nodes;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.PathArgument;
import org.opendaylight.yangtools.yang.data.api.schema.DataContainerChild;
import org.opendaylight.yangtools.yang.data.api.schema.LeafNode;
import org.opendaylight.yangtools.yang.data.impl.schema.ImmutableNodes;

/**
 * Support utilities for dealing with Maps which would normally hold {@link DataContainerChild} values, but are modified
 * to eliminate {@link LeafNode} instances.
 */
@Beta
public final class LazyLeafOperations {
    private LazyLeafOperations() {
        // Hidden on purpose
    }

    public static @Nullable DataContainerChild<?, ?> getChild(final Map<PathArgument, Object> map,
            final PathArgument key) {
        final Object value = map.get(key);
        return value == null ? null : decodeChild(key, value);
    }

    public static void putChild(final Map<PathArgument, Object> map, final DataContainerChild<?, ?> child) {
        final DataContainerChild<?, ?> node = requireNonNull(child);
        map.put(node.getIdentifier(), encodeExpendableChild(node));
    }

    static @NonNull LeafNode<?> coerceLeaf(final PathArgument key, final Object value) {
        verify(key instanceof NodeIdentifier, "Unexpected value %s for child %s", value, key);
        return ImmutableNodes.leafNode((NodeIdentifier) key, value);
    }

    private static @Nullable DataContainerChild<?, ?> decodeChild(final PathArgument key, final @NonNull Object value) {
        return decodeExpendableChild(key, value);
    }

    private static @NonNull DataContainerChild<?, ?> decodeExpendableChild(final PathArgument key,
            @NonNull final Object value) {
        return value instanceof DataContainerChild ? (DataContainerChild<?, ?>) value : coerceLeaf(key, value);
    }

    private static @NonNull Object encodeExpendableChild(final @NonNull DataContainerChild<?, ?> node) {
        return node instanceof LeafNode ? verifyEncode(((LeafNode<?>) node).getValue()) : node;
    }

    private static @NonNull Object verifyEncode(final @NonNull Object value) {
        verify(!(value instanceof DataContainerChild), "Unexpected leaf value %s", value);
        return value;
    }
}
