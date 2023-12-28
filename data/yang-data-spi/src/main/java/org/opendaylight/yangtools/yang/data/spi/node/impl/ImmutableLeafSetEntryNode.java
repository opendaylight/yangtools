/*
 * Copyright (c) 2023 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.node.impl;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.api.YangInstanceIdentifier.NodeWithValue;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractLeafSetEntryNode;

public abstract sealed class ImmutableLeafSetEntryNode<T> extends AbstractLeafSetEntryNode<T> {
    private static final class Binary extends ImmutableLeafSetEntryNode<byte[]> {
        Binary(final NodeWithValue<byte[]> name) {
            super(name);
        }

        @Override
        protected byte[] wrappedValue() {
            return value().clone();
        }
    }

    private static final class Regular<T> extends ImmutableLeafSetEntryNode<T> {
        Regular(final NodeWithValue<T> name) {
            super(name);
        }

        @Override
        protected T wrappedValue() {
            return value();
        }
    }

    private final @NonNull NodeWithValue<T> name;

    private ImmutableLeafSetEntryNode(final NodeWithValue<T> name) {
        this.name = requireNonNull(name);
    }

    public static <T> @NonNull ImmutableLeafSetEntryNode<T> of(final NodeWithValue<T> name) {
        if (name.getValue() instanceof byte[]) {
            @SuppressWarnings("unchecked")
            final var ret = (ImmutableLeafSetEntryNode<T>) new Binary((NodeWithValue<byte[]>) name);
            return ret;
        }
        return new Regular<>(name);
    }

    public static <T> @NonNull ImmutableLeafSetEntryNode<T> of(final NodeWithValue<T> name, final T body) {
        final var nameValue = name.getValue();
        if (body instanceof byte[] bodyBytes) {
            if (nameValue instanceof byte[] nameBytes && Arrays.equals(nameBytes, bodyBytes)) {
                @SuppressWarnings("unchecked")
                final var ret = (ImmutableLeafSetEntryNode<T>) new Binary((NodeWithValue<byte[]>) name);
                return ret;
            }
        } else if (nameValue.equals(body)) {
            return new Regular<>(name);
        }

        throw new IllegalArgumentException(
            "Node identifier contains different value: " + name + " than value itself: " + body);
    }

    @Override
    public final NodeWithValue<T> name() {
        return name;
    }

    @Override
    protected final T value() {
        return name.getValue();
    }
}
