/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.util.codec;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedDataSchemaNode;

/**
 * Abstract cache for codecs.
 *
 * @author Robert Varga
 *
 * @param <T> Codec type
 */
@Beta
public abstract class CodecCache<T> {
    /**
     * Lookup a complex codec for schema node.
     *
     * @param schema Schema node
     * @return Cached codec, or null if no codec is cached.
     */
    abstract @Nullable T lookupComplex(TypedDataSchemaNode schema);

    /**
     * Lookup a simple codec for a type definition.
     *
     * @param type Type definition
     * @return Cached codec, or null if no codec is cached.
     */
    abstract @Nullable T lookupSimple(TypeDefinition<?> type);

    /**
     * Lookup-or-store a complex codec for a particular schema node.
     *
     * @param schema Schema node
     * @param codec Codec to cache
     * @return Codec instance, either already-cached, or the codec presented as argument.
     */
    abstract @NonNull T getComplex(TypedDataSchemaNode schema, T codec);

    /**
     * Lookup-or-store a simple codec for a particular schema node.
     *
     * @param type Type definition
     * @param codec Codec to cache
     * @return Codec instance, either already-cached, or the codec presented as argument.
     */
    abstract @NonNull T getSimple(TypeDefinition<?> type, T codec);
}
