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
import org.opendaylight.yangtools.yang.common.Uint64;

public final class Uint64Serializer extends StdSerializer<Uint64> {
    private static final long serialVersionUID = 1L;

    public Uint64Serializer() {
        super(Uint64.class);
    }

    @Override
    public void serialize(final Uint64 value, final JsonGenerator gen, final SerializerProvider provider)
            throws IOException {
        // https://tools.ietf.org/html/rfc7951#section-6.1
        gen.writeString(value.toCanonicalString());
    }
}
