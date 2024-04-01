/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

final class NullJSONCodec implements JSONCodec<Object> {
    static final NullJSONCodec INSTANCE = new NullJSONCodec();
    private static final Logger LOG = LoggerFactory.getLogger(NullJSONCodec.class);

    private NullJSONCodec() {

    }

    @Override
    public Class<Object> getDataType() {
        return Object.class;
    }

    @Override
    public Object parseValue(final String str) {
        LOG.warn("Call of the deserializeString method on null codec. No operation performed.");
        return null;
    }

    @Override
    public void writeValue(final JsonWriter ctx, final Object value) throws IOException {
        // NOOP since codec is unknown.
        LOG.warn("Call of the serializeToWriter method on null codec. No operation performed.");
    }
}
