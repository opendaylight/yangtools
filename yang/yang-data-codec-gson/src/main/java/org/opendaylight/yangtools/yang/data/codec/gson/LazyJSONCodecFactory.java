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
import org.opendaylight.yangtools.yang.model.api.TypedSchemaNode;

/**
 * Lazily-computed JSONCodecFactory. This is a non-thread-safe factory, which performs caching of codecs. It is most
 * appropriate for one-off encodings of repetetive data.
 *
 * @author Robert Varga
 */
@NotThreadSafe
final class LazyJSONCodecFactory extends JSONCodecFactory {
    private final Map<TypedSchemaNode, JSONCodec<?>> codecs = new IdentityHashMap<>();

    LazyJSONCodecFactory(final SchemaContext context) {
        super(context);
    }

    @Override
    JSONCodec<?> codecFor(final TypedSchemaNode schema) {
        return codecs.computeIfAbsent(schema, node -> createCodec(schema, schema.getType()));
    }

    // Used by EagerJSONCodecFactory. The map is leaked as-is, as we do not expect it to be touched again.
    Map<TypedSchemaNode, JSONCodec<?>> getCodecs() {
        return codecs;
    }
}
