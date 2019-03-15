/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.google.common.annotations.Beta;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;

/**
 * A no-operation codec cache.
 *
 * @author Robert Varga
 *
 * @param <T> Codec type
 */
@Beta
public final class NoopCodecCache<T> extends CodecCache<T> {
    private static final NoopCodecCache<?> INSTANCE = new NoopCodecCache<>();

    private NoopCodecCache() {
        // Hidden
    }

    @SuppressWarnings("unchecked")
    public static <T> NoopCodecCache<T> getInstance() {
        return (NoopCodecCache<T>) INSTANCE;
    }

    @Override
    T lookupComplex(final TypedDataSchemaNode schema) {
        return null;
    }

    @Override
    T lookupSimple(final TypeDefinition<?> type) {
        return null;
    }

    @Override
    T getSimple(final TypeDefinition<?> type, final T codec) {
        return codec;
    }

    @Override
    T getComplex(final TypedDataSchemaNode schema, final T codec) {
        return codec;
    }
}
