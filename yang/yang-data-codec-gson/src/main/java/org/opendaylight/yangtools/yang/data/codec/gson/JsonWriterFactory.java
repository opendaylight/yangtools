/**
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.gson.stream.JsonWriter;
import java.io.Writer;

/**
 * Factory Method class for JsonWriter creation.
 */
@Beta
public final class JsonWriterFactory {
    private JsonWriterFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Create a new JsonWriter, which writes to the specified output writer.
     *
     * @param writer Output writer
     * @return A JsonWriter instance
     */
    public static JsonWriter createJsonWriter(final Writer writer) {
        return new JsonWriter(writer);
    }

    /**
     * Create a new JsonWriter, which writes to the specified output writer.
     *
     * @param writer Output writer
     * @param indentSize size of the indent
     * @return A JsonWriter instance
     */
    public static JsonWriter createJsonWriter(final Writer writer, final int indentSize) {
        JsonWriter jsonWriter = new JsonWriter(writer);
        final String indent = Strings.repeat(" ", indentSize);
        jsonWriter.setIndent(indent);
        return jsonWriter;
    }
}
