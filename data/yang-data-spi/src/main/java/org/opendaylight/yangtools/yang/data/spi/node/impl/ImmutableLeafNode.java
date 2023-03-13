/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeIdentifier;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractLeafNode;

public abstract sealed class ImmutableLeafNode<T> extends AbstractLeafNode<T> {
    private static final class Regular<T> extends ImmutableLeafNode<T> {
        Regular(final NodeIdentifier name, final T value) {
            super(name, value);
        }

        @Override
        protected T wrappedValue() {
            return value();
        }
    }

    private static final class Binary extends ImmutableLeafNode<byte[]> {
        Binary(final NodeIdentifier name, final byte[] value) {
            super(name, value);
        }

        @Override
        protected byte[] wrappedValue() {
            return value().clone();
        }
    }

    private final @NonNull NodeIdentifier name;
    private final @NonNull T value;

    private ImmutableLeafNode(final NodeIdentifier name, final T value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }

    public static <T> @NonNull ImmutableLeafNode<T> of(final NodeIdentifier identifier, final T value) {
        if (value instanceof byte[] bytes) {
            @SuppressWarnings("unchecked")
            final var ret = (ImmutableLeafNode<T>) new Binary(identifier, bytes);
            return ret;
        } else if (value instanceof YangInstanceIdentifier yiid && yiid.isEmpty()) {
            throw new IllegalArgumentException("Leaf node value cannot be an empty instance identifier");
        }
        return new Regular<>(identifier, value);
    }

    @Override
    public final NodeIdentifier name() {
        return name;
    }

    @Override
    protected final T value() {
        return value;
    }
}
