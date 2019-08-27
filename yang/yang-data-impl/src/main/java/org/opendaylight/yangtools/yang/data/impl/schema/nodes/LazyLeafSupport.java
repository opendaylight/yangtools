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
import java.util.Map.Entry;
import java.util.Optional;
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
public final class LazyLeafSupport {
    private LazyLeafSupport() {

    }

    public static @NonNull Object encodeChild(final @NonNull DataContainerChild<?, ?> child) {
        return child instanceof LeafNode ? ((LeafNode<?>) child).getValue() : requireNonNull(child);
    }

    public static @Nullable DataContainerChild<? extends PathArgument, ?> decodeChild(
            final @NonNull PathArgument key, final @Nullable Object encoded) {
        if (encoded instanceof DataContainerChild) {
            return (DataContainerChild<?, ?>)encoded;
        }
        return encoded == null ? null : coerceLeaf(key, encoded);
    }

    public static Optional<DataContainerChild<? extends PathArgument, ?>> decodeChildOptional(
            final @NonNull PathArgument key, final @Nullable Object encoded) {
        if (encoded instanceof DataContainerChild) {
            return Optional.of((DataContainerChild<?, ?>)encoded);
        }
        return encoded == null ? Optional.empty() : Optional.of(coerceLeaf(key, encoded));
    }

    public static @NonNull DataContainerChild<? extends PathArgument, ?> decodeEntry(
            final @NonNull Entry<PathArgument, Object> entry) {
        final Object value = entry.getValue();
        return value instanceof DataContainerChild ? (DataContainerChild<?, ?>) value
                : coerceLeaf(entry.getKey(), value);
    }

    private static LeafNode<?> coerceLeaf(final PathArgument key, final Object value) {
        verify(key instanceof NodeIdentifier, "Unexpected value %s for child %s", value, key);
        return ImmutableNodes.leafNode((NodeIdentifier) key, value);
    }
}
