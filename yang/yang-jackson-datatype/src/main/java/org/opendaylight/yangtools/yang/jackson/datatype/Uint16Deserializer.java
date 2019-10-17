/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.Uint16;

final class Uint16Deserializer extends StdScalarDeserializer<Uint16> {
    private static final long serialVersionUID = 1L;

    static final Uint16Deserializer INSTANCE = new Uint16Deserializer();

    private Uint16Deserializer() {
        super(Uint16.class);
    }

    @Override
    public Uint16 deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return Uint16.valueOf(p.getIntValue());
        }
        return null;
    }
}
