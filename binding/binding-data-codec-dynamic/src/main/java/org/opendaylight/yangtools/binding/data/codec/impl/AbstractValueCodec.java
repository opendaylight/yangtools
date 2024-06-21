/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.data.codec.impl;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

abstract class AbstractValueCodec<S, D> implements ValueCodec<S, D> {
    @Override
    public final D deserialize(final S input) {
        return verifyResult(deserializeImpl(requireNonNull(input)), input);
    }

    @Override
    public final S serialize(final D input) {
        return verifyResult(serializeImpl(requireNonNull(input)), input);
    }

    // implementation is guarded from nulls and verified not to return null
    abstract @NonNull D deserializeImpl(@NonNull S product);

    // implementation is guarded from nulls and verified not to return null
    abstract @NonNull S serializeImpl(@NonNull D input);

    private <X> X verifyResult(final @Nullable X result, final Object input) {
        return verifyNotNull(result, "Codec %s returned null on %s", this, input);
    }
}
