/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableMap;
import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.BuiltInType;
import org.opendaylight.yangtools.yang.data.api.schema.NormalizedNode;

/**
 * A shared {@link SchemaUnawareCodec} backed by a {@link BuiltInType}. This has a few well-known instances, where
 * Java YANG Binding uses same types as {@link NormalizedNode} model for base YANG types, representing numbers,
 * strings, booleans, binary and empty.
 */
final class BuiltInValueCodec extends SchemaUnawareCodec {
    private static final ImmutableMap<Class<?>, BuiltInValueCodec> SINGLETONS = List.of(
        BuiltInType.INT8,
        BuiltInType.INT16,
        BuiltInType.INT32,
        BuiltInType.INT64,
        BuiltInType.UINT8,
        BuiltInType.UINT16,
        BuiltInType.UINT32,
        BuiltInType.UINT64,
        BuiltInType.DECIMAL64,
        BuiltInType.STRING,
        BuiltInType.BOOLEAN,
        BuiltInType.BINARY,
        BuiltInType.EMPTY).stream()
        .map(BuiltInValueCodec::new)
        .collect(ImmutableMap.toImmutableMap(codec -> codec.type.javaClass(), x -> x));

    private final @NonNull BuiltInType<?> type;

    private BuiltInValueCodec(final BuiltInType<?> type) {
        this.type = requireNonNull(type);
    }

    static @Nullable BuiltInValueCodec forValueType(final Class<?> valueType) {
        return SINGLETONS.get(valueType);
    }

    @Override
    Object deserializeImpl(final Object product) {
        return type.castValue(product);
    }

    @Override
    Object serializeImpl(final Object input) {
        return type.castValue(input);
    }
}
