/*
 * Copyright (c) 2017 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.util.IdentityHashMap;
import java.util.Map;
import javax.annotation.concurrent.NotThreadSafe;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;

/**
 * Lazily-computed JSONCodecFactory. This is a non-thread-safe factory, which performs caching of codecs. It is most
 * appropriate for one-off encodings of repetetive data.
 *
 * @author Robert Varga
 */
@NotThreadSafe
final class LazyJSONCodecFactory extends JSONCodecFactory {
    private final Map<TypedSchemaNode, JSONCodec<?>> complexCodecs = new IdentityHashMap<>();
    private final Map<TypeDefinition<?>, JSONCodec<?>> simpleCodecs = new IdentityHashMap<>();

    LazyJSONCodecFactory(final SchemaContext context) {
        super(context);
    }

    @Override
    JSONCodec<?> getComplex(final TypedSchemaNode schema, final JSONCodec<?> codec) {
        return getComplexCodecs().computeIfAbsent(schema, any -> codec);
    }

    @Override
    JSONCodec<?> lookupComplex(final TypedSchemaNode schema) {
        return getComplexCodecs().get(schema);
    }

    @Override
    JSONCodec<?> lookupSimple(final TypeDefinition<?> type) {
        return getSimpleCodecs().get(type);
    }

    @Override
    JSONCodec<?> getSimple(final TypeDefinition<?> type, final JSONCodec<?> codec) {
        return getSimpleCodecs().computeIfAbsent(type, any -> codec);
    }

    Map<TypeDefinition<?>, JSONCodec<?>> getSimpleCodecs() {
        return simpleCodecs;
    }

    Map<TypedSchemaNode, JSONCodec<?>> getComplexCodecs() {
        return complexCodecs;
    }
}
