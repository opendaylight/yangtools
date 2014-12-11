/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import com.google.common.annotations.Beta;
import com.google.common.base.Strings;
import com.google.gson.stream.JsonWriter;
import java.io.Writer;

/**
 * Factory Method class for JSONWriter creation
 */
@Beta
public class JsonWriterFactory {

    public static JsonWriter createJsonWriter(Writer writer) {
        return new JsonWriter(writer);
    }

    public static JsonWriter createJsonWriter(Writer writer, int indentSize) {
        JsonWriter adapter = new JsonWriter(writer);
        final String indent = Strings.repeat(" ", indentSize);
        adapter.setIndent(indent);
        return adapter;
    }

}
