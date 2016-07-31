/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.base.Preconditions;
import org.opendaylight.yangtools.concepts.Codec;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.data.api.codec.Int16Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int32Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int64Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Int8Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint16Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint32Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.data.api.codec.Uint8Codec;
import org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodec;

/**
 * Abstract base implementation of {@link JSONCodec}, which wraps a {@link TypeDefinitionAwareCodec}.
 *
 * @param <T> Deserialized objec type
 */
abstract class AbstractJSONCodec<T> implements JSONCodec<T> {
    private final Codec<String, T> codec;

    protected AbstractJSONCodec(final Codec<String, T> codec) {
        this.codec = Preconditions.checkNotNull(codec);
    }

    /**
     * Create a proper JSONCodec based on the underlying codec type
     * @param codec underlying codec
     * @return A JSONCodec instance
     */
    public static JSONCodec<?> create(final Codec<String, ?> codec) {
        if (codec instanceof BooleanCodec) {
            return new BooleanJSONCodec((BooleanCodec<String>) codec);
        }
        if (codec instanceof DecimalCodec || codec instanceof Int8Codec
                || codec instanceof Int16Codec || codec instanceof Int32Codec
                || codec instanceof Int64Codec || codec instanceof Uint8Codec
                || codec instanceof Uint16Codec || codec instanceof Uint32Codec
                || codec instanceof Uint64Codec) {
            return new NumberJSONCodec(codec);
        }

        return new QuotedJSONCodec<>(codec);
    }

    @Override
    public final T deserialize(final String input) {
        return codec.deserialize(input);
    }

    @Override
    public final String serialize(final T input) {
        return codec.serialize(input);
    }
}
