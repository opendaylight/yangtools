/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.jackson.datatype;

import static java.util.Objects.requireNonNull;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.exc.InputCoercionException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class AbstractDeserializer<T> extends StdScalarDeserializer<T> {
    private static final Logger LOG = LoggerFactory.getLogger(AbstractDeserializer.class);
    private static final long serialVersionUID = 1L;

    private final @NonNull JsonToken token;

    AbstractDeserializer(final Class<T> valueType, final JsonToken token) {
        super(valueType);
        this.token = requireNonNull(token);
    }

    @Override
    public final T deserialize(final JsonParser p, final DeserializationContext ctxt) throws IOException {
        if (!p.hasToken(token)) {
            return (T) ctxt.handleUnexpectedToken(handledType(), p.currentToken(), p,
                "Expected a `JsonToken.%s`, got `JsonToken.%s`", p.currentToken());
        }
        try {
            return deserializeToken(p);
        } catch (IllegalArgumentException e) {
            LOG.debug("Failed to parse as {}", handledType(), e);


            throw new InputCoercionException(p, e.getMessage(), token, handledType());
        }
    }

    abstract T deserializeToken(@NonNull JsonParser parser) throws IOException;
}
