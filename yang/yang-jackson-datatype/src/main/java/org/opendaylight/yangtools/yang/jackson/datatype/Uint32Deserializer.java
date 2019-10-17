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
import org.opendaylight.yangtools.yang.common.Uint32;

final class Uint32Deserializer extends StdScalarDeserializer<Uint32> {
    private static final long serialVersionUID = 1L;

    static final Uint32Deserializer INSTANCE = new Uint32Deserializer();

    private Uint32Deserializer() {
        super(Uint32.class);
    }

    @Override
    public Uint32 deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        if (p.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            return Uint32.valueOf(p.getLongValue());
        }
        return null;
    }
}
