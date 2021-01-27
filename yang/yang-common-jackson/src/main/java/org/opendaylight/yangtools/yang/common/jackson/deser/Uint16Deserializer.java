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
import org.opendaylight.yangtools.yang.common.Uint16;

public final class Uint16Deserializer extends StdScalarDeserializer<Uint16> {
    private static final long serialVersionUID = 1L;

    public Uint16Deserializer() {
        super(Uint16.class);
    }

    @Override
    @SuppressWarnings({ "checkstyle:ParameterName", "checkstyle:AvoidHidingCauseException" })
    public Uint16 deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        final int intVal = _parseIntPrimitive(p, ctxt);
        try {
            return Uint16.valueOf(intVal);
        } catch (IllegalArgumentException e) {
            throw MismatchedInputException.from(p, Uint16.class, e.getMessage());
        }
    }
}
