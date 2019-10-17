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
import org.opendaylight.yangtools.yang.common.Uint16;

final class Uint16Deserializer extends AbstractDeserializer<Uint16> {
    private static final long serialVersionUID = 1L;

    static final Uint16Deserializer INSTANCE = new Uint16Deserializer();

    private Uint16Deserializer() {
        super(Uint16.class, JsonToken.VALUE_NUMBER_INT);
    }

    @Override
    Uint16 deserializeToken(final JsonParser parser) throws IOException {
        return Uint16.valueOf(parser.getIntValue());
    }
}
