/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import org.opendaylight.yangtools.yang.data.impl.codec.DataStringCodec;

/**
 * A {@link JSONCodec} which does not need double quotes in output representation.
 *
 * @param <T> Deserialized value type
 */
final class NumberJSONCodec<T extends Number> extends AbstractJSONCodec<T> {
    NumberJSONCodec(final DataStringCodec<T> codec) {
        super(codec);
    }

    @Override
    public void writeValue(final JsonWriter ctx, final T value) throws IOException {
        ctx.value(value);
    }
}