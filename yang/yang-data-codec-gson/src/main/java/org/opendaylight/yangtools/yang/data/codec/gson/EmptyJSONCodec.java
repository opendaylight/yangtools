/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;

final class EmptyJSONCodec implements JSONCodec<Void> {

    static final EmptyJSONCodec INSTANCE = new EmptyJSONCodec();

    private EmptyJSONCodec() {

    }

    @Override
    public Class<Void> getDataType() {
        return Void.class;
    }

    @Override
    public Void parseValue(final Object ctx, final String input) {
        return null;
    }

    @Override
    public void writeValue(final JsonWriter ctx, final Void value) throws IOException {
        ctx.beginArray();
        ctx.value((String) null);
        ctx.endArray();
    }
}
