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
import org.opendaylight.yangtools.yang.common.Empty;

final class EmptyJSONCodec implements JSONCodec<Empty> {
    static final EmptyJSONCodec INSTANCE = new EmptyJSONCodec();

    private EmptyJSONCodec() {
        // Hidden on purpose
    }

    @Override
    public Class<Empty> getDataType() {
        return Empty.class;
    }

    @Override
    public Empty parseValue(final String input) {
        return Empty.value();
    }

    @Override
    public void writeValue(final JsonWriter ctx, final Empty value) throws IOException {
        ctx.beginArray();
        ctx.value((String) null);
        ctx.endArray();
    }
}
