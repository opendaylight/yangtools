/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationException;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 *
 */
public abstract class AbstractStringConverter<T, D extends TypeDefinition<D>> implements StringConverter {
    private final @NonNull Class<T> valueType;
    private final @NonNull D typedef;

    protected AbstractStringConverter(final Class<T> valueType, final D typedef) {
        this.valueType = requireNonNull(valueType);
        this.typedef = requireNonNull(typedef);
    }

    @Override
    public final T normalizeFromString(final String str) throws NormalizationException {
        final var nonnull = requireNonNull(str);
        return verifyResult(normalizeFromString(typedef, nonnull), nonnull);
    }

    // implementation is guarded from nulls and verified not to return null
    @NonNullByDefault
    protected abstract T normalizeFromString(D typedef, String str) throws NormalizationException;

    @Override
    public final String canonizeToString(final Object obj) throws NormalizationException {
        final @NonNull T cast;
        try {
            cast = requireNonNull(valueType.cast(obj));
        } catch (ClassCastException e) {
            throw NormalizationException.ofCause(e);
        }
        return verifyResult(canonizeToString(typedef, cast), cast);
    }

    // implementation is guarded from nulls and verified not to return null
    @NonNullByDefault
    protected abstract String canonizeToString(D typedef, T obj) throws NormalizationException;

    private <X> @NonNull X verifyResult(final @Nullable X result, final Object input) throws NormalizationException {
        if (result == null) {
            throw NormalizationException.ofMessage("Codec " + this + " returned null on " + input);
        }
        return result;
    }
}
