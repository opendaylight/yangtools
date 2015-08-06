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

final class JSONEmptyCodec implements JSONCodec<Object> {

    static final JSONEmptyCodec INSTANCE = new JSONEmptyCodec();

    private JSONEmptyCodec() {

    }

    @Override
    public Object deserialize(final String input) {
        return null;
    }

    @Override
    public String serialize(final Object input) {
        return null;
    }

    @Override
    public boolean needQuotes() {
        return false;
    }

    @Override
    public void serializeToWriter(final JsonWriter writer, final Object value) throws IOException {
        writer.beginArray();
        writer.value((String) null);
        writer.endArray();
    }

}
