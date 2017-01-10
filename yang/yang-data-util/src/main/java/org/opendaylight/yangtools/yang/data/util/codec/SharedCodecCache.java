/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.google.common.annotations.Beta;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.util.concurrent.ExecutionException;
import javax.annotation.concurrent.ThreadSafe;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;

/**
 * A thread-safe lazily-populated codec cache. Instances are cached in an internal weak/soft cache.
 *
 * @author Robert Varga
 */
@Beta
@ThreadSafe
public final class SharedCodecCache<T> extends CodecCache<T> {
    // Weak keys to force identity lookup
    // Soft values to keep unreferenced codecs around for a bit, but eventually we want them to go away
    private final Cache<TypeDefinition<?>, T> simpleCodecs = CacheBuilder.newBuilder().weakKeys().softValues().build();
    private final Cache<TypedSchemaNode, T> complexCodecs = CacheBuilder.newBuilder().weakKeys().softValues().build();

    @Override
    public T lookupComplex(final TypedSchemaNode schema) {
        return complexCodecs.getIfPresent(schema);
    }

    @Override
    T lookupSimple(final TypeDefinition<?> type) {
        return simpleCodecs.getIfPresent(type);
    }

    @Override
    T getComplex(final TypedSchemaNode schema, final T codec) {
        try {
            return complexCodecs.get(schema, () -> codec);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            Throwables.throwIfUnchecked(cause);
            throw new RuntimeException(e);
        }
    }

    @Override
    T getSimple(final TypeDefinition<?> type, final T codec) {
        try {
            return simpleCodecs.get(type, () -> codec);
        } catch (ExecutionException e) {
            final Throwable cause = e.getCause();
            Throwables.throwIfUnchecked(cause);
            throw new RuntimeException(e);
        }
    }
}
