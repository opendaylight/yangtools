/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson.ser;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;
import org.opendaylight.yangtools.yang.common.Uint16;

public final class Uint16Serializer extends StdSerializer<Uint16> {
    private static final long serialVersionUID = 1L;

    public Uint16Serializer() {
        super(Uint16.class);
    }

    @Override
    public void serialize(final Uint16 value, final JsonGenerator gen, final SerializerProvider provider)
            throws IOException {
        gen.writeNumber(value.toJava());
    }
}
