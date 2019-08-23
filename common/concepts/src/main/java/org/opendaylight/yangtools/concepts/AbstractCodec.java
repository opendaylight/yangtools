/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.concepts;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * An abstract base class enforcing nullness contract around {@link Codec} interface.
 *
 * @param <P> Product type
 * @param <I> Input type
 * @param <X> Error exception type
 */
@Beta
@NonNullByDefault
public abstract class AbstractCodec<P, I, X extends Exception> implements Codec<P, I, X> {
    @Override
    public final @NonNull I deserialize(@NonNull final P input) throws X {
        return verifyNotNull(deserializeImpl(requireNonNull(input)), "Codec %s returned null on %s", this, input);
    }

    @Override
    public final @NonNull P serialize(@NonNull final I input) throws X {
        return verifyNotNull(serializeImpl(requireNonNull(input)), "Codec %s returned null on %s", this, input);
    }

    // implementation is guarded from nulls and verified not to return null
    protected abstract @NonNull I deserializeImpl(@NonNull P product) throws X;

    // implementation is guarded from nulls and verified not to return null
    protected abstract @NonNull P serializeImpl(@NonNull I input) throws X;
}
