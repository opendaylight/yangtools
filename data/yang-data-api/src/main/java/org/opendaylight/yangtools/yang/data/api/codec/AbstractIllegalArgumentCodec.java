/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.api.codec;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An abstract base class enforcing nullness contract around {@link IllegalArgumentCodec} interface.
 *
 * @param <S> Serializied (external) type
 * @param <D> Deserialized (internal) type
 */
@Beta
public abstract non-sealed class AbstractIllegalArgumentCodec<S, D> implements IllegalArgumentCodec<S, D> {
    @Override
    public final D deserialize(final S input) {
        return verifyResult(deserializeImpl(requireNonNull(input)), input);
    }

    @Override
    public final S serialize(final D input) {
        return verifyResult(serializeImpl(requireNonNull(input)), input);
    }

    // implementation is guarded from nulls and verified not to return null
    protected abstract @NonNull D deserializeImpl(@NonNull S product);

    // implementation is guarded from nulls and verified not to return null
    protected abstract @NonNull S serializeImpl(@NonNull D input);

    private <X> X verifyResult(final @Nullable X result, final Object input) {
        return verifyNotNull(result, "Codec %s returned null on %s", this, input);
    }
}
