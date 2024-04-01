/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.data.util.codec.TypeAwareCodec;

/**
 * A codec capable of performing normalized value conversion with a {@link JsonWriter}.
 *
 * @param <T> Normalized value type
 */
public sealed interface JSONCodec<T> extends TypeAwareCodec<T, Void, JsonWriter>
        permits AbstractJSONCodec, EmptyJSONCodec, IdentityrefJSONCodec, InstanceIdentifierJSONCodec, UnionJSONCodec,
                NullJSONCodec {
    /**
     * {@inheritDoc}.
     *
     * @throws IOException if the write fails
     * @deprecated Use {@link #writeValue(JSONValueWriter, Object)} instead.
     */
    @Override
    @Deprecated(since = "13.0.3", forRemoval = true)
    default void writeValue(final JsonWriter writer, final T value) throws IOException {
        writeValue(new DefaultJSONValueWriter(writer), value);
    }

    /**
     * Serialize specified value with specified {@link JSONValueWriter}.
     *
     * @param ctx Write context
     * @param value Value in native format
     * @throws IOException if the write fails
     */
    void writeValue(JSONValueWriter ctx, T value) throws IOException;

    /**
     * {@inheritDoc}.
     *
     * @deprecated Use {@link #parseValue(String)} instead.
     */
    @Override
    @Deprecated
    default T parseValue(final Void ctx, final String str) {
        return parseValue(str);
    }

    /**
     * Parse a String representation into its native format.
     *
     * @param str String representation
     * @return Value in native format
     * @throws IllegalArgumentException if the value does not parse or pass type validation
     */
    T parseValue(String str);

    /**
     * Return the {@link JSONValue} representation of a native value.
     *
     * @param value Value in native format
     * @return A {@link JSONValue}
     * @throws IllegalArgumentException if the value does not parse or pass type validation
     */
    @NonNull JSONValue unparseValue(T value);
}