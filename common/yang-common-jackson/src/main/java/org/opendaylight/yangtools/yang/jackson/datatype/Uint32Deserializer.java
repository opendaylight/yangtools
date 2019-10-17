/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.jackson.datatype;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.Uint32;

final class Uint32Deserializer extends AbstractDeserializer<Uint32> {
    private static final long serialVersionUID = 1L;

    static final Uint32Deserializer INSTANCE = new Uint32Deserializer();

    private Uint32Deserializer() {
        super(Uint32.class, JsonToken.VALUE_NUMBER_INT);
    }

    @Override
    Uint32 deserializeToken(final JsonParser parser) throws IOException {
        return Uint32.valueOf(parser.getLongValue());
    }
}
