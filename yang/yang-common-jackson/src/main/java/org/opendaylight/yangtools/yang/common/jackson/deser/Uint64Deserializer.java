/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson.deser;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.Uint64;

public final class Uint64Deserializer extends StdScalarDeserializer<Uint64> {
    private static final long serialVersionUID = 1L;

    public Uint64Deserializer() {
        super(Uint64.class);
    }

    @Override
    @SuppressWarnings({ "checkstyle:ParameterName", "checkstyle:AvoidHidingCauseException" })
    public Uint64 deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        // https://tools.ietf.org/html/rfc7951#section-6.1
        final String str = _parseString(p, ctxt);
        try {
            return Uint64.valueOf(str);
        } catch (IllegalArgumentException e) {
            throw MismatchedInputException.from(p, Uint64.class, e.getMessage());
        }
    }
}
