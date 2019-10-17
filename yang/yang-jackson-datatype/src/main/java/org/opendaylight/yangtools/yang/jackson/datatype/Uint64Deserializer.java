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
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.Uint64;

final class Uint64Deserializer extends StdScalarDeserializer<Uint64> {
    private static final long serialVersionUID = 1L;

    static final Uint64Deserializer INSTANCE = new Uint64Deserializer();

    private Uint64Deserializer() {
        super(Uint64.class);
    }

    @Override
    public Uint64 deserialize(final JsonParser p, final DeserializationContext ctxt)
            throws IOException, JsonProcessingException {
        // TODO Auto-generated method stub
        return null;
    }
}
