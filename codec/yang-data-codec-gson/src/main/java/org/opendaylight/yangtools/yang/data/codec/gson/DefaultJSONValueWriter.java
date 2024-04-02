/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

/**
 * A {@link JSONValueWriter} backed by a {@link JsonWriter}.
 */
public final class DefaultJSONValueWriter implements JSONValueWriter {
    private final JsonWriter writer;

    public DefaultJSONValueWriter(final JsonWriter writer) {
        this.writer = requireNonNull(writer);
    }

    @Override
    public void writeBoolean(final boolean value) throws IOException {
        writer.value(value);
    }

    @Override
    public void writeEmpty() throws IOException {
        writer.beginArray().nullValue().endArray();
    }

    @Override
    public void writeNumber(final Number value) throws IOException {
        writer.value(requireNonNull(value));
    }

    @Override
    public void writeString(final String value) throws IOException {
        writer.value(requireNonNull(value));
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("writer", writer).toString();
    }
}
