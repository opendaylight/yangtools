/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableList;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.codec.UnionCodec;
import org.opendaylight.yangtools.yang.model.api.type.UnionTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class UnionStringCodec extends TypeDefinitionAwareCodec<Object, UnionTypeDefinition>
        implements UnionCodec<String> {
    private static final Logger LOG = LoggerFactory.getLogger(UnionStringCodec.class);

    private final ImmutableList<TypeDefinitionAwareCodec<Object, ?>> codecs;

    private UnionStringCodec(final UnionTypeDefinition typeDef,
            final ImmutableList<TypeDefinitionAwareCodec<Object, ?>> codecs) {
        super(Object.class, typeDef);
        this.codecs = requireNonNull(codecs);
    }

    static @Nullable TypeDefinitionAwareCodec<?, UnionTypeDefinition> from(final UnionTypeDefinition typeDef) {
        final var types = typeDef.getTypes();
        final var builder = ImmutableList.<TypeDefinitionAwareCodec<Object, ?>>builderWithExpectedSize(types.size());
        for (var type : types) {
            final var codec = from(type);
            if (codec == null) {
                LOG.debug("Cannot handle {} because of unhandled component {}", typeDef, type);
                return null;
            }
            builder.add(codec);
        }
        return new UnionStringCodec(typeDef, builder.build());
    }

    @Override
    protected Object deserializeImpl(final String stringRepresentation) {
        List<IllegalArgumentException> suppressed = null;
        for (var codec : codecs) {
            try {
                return codec.deserialize(stringRepresentation);
            } catch (final IllegalArgumentException e) {
                // invalid - try the next union type.
                LOG.debug("Value {} did not match codec {}", stringRepresentation, codec, e);
                if (suppressed == null) {
                    suppressed = new ArrayList<>();
                }
                suppressed.add(e);
            }
        }

        final var ex = new IllegalArgumentException("Invalid value \"" + stringRepresentation + "\" for union type.");
        if (suppressed != null) {
            suppressed.forEach(ex::addSuppressed);
        }
        throw ex;
    }

    @Override
    protected String serializeImpl(final Object data) {
        return data instanceof byte[] bytes ? Base64.getEncoder().encodeToString(bytes) : data.toString();
    }
}
