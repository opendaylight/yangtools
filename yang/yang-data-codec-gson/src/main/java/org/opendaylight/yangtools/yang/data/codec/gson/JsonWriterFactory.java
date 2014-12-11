/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.gson.stream.JsonWriter;
import java.io.Writer;

/**
 * Factory Method class for JsonWriter creation
 */
@Beta
public class JsonWriterFactory {

    /**
     * Create a new JsonWriter, which writes to the specified output writer.
     *
     * @param writer Output writer
     * @return A JsonWriter instance
     */
    public static JsonWriter createJsonWriter(Writer writer) {
        return new JsonWriter(writer);
    }

    /**
     * Create a new JsonWriter, which writes to the specified output writer.
     *
     * @param writer Output writer
     * @param indentSize size of the indent
     * @return A JsonWriter instance
     */
    public static JsonWriter createJsonWriter(Writer writer, int indentSize) {
        JsonWriter jsonWriter = new JsonWriter(writer);
        final String indent = Strings.repeat(" ", indentSize);
        jsonWriter.setIndent(indent);
        return jsonWriter;
    }

}
