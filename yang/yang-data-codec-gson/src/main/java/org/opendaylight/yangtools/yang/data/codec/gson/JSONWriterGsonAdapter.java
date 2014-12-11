/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.io.IOException;
import java.io.Writer;

import com.google.gson.stream.JsonWriter;

/**
 * Adapter class for com.google.gson.stream.JsonWriter
 */
public class JSONWriterGsonAdapter implements JSONWriter {
    private JsonWriter jsonWriter;

    public JSONWriterGsonAdapter(Writer writer) {
        this.jsonWriter = new JsonWriter(writer);
    }

    public void setIndent(String indent) {
        jsonWriter.setIndent(indent);
    }

    public void beginArray() throws IOException {
        jsonWriter.beginArray();
    }

    public void endArray() throws IOException {
        jsonWriter.endArray();
    }

    public void beginObject() throws IOException {
        jsonWriter.beginObject();
    }

    public void endObject() throws IOException {
        jsonWriter.endObject();
    }

    public void name(String name) throws IOException {
        jsonWriter.name(name);
    }

    public void value(String value) throws IOException {
        jsonWriter.value(value);
    }

    public void nullValue() throws IOException {
        jsonWriter.nullValue();
    }

    public void value(boolean value) throws IOException {
        jsonWriter.value(value);
    }

    public void value(double value) throws IOException {
        jsonWriter.value(value);
    }

    public void value(long value) throws IOException {
        jsonWriter.value(value);
    }

    public void value(Number value) throws IOException {
        jsonWriter.value(value);
    }

    public void flush() throws IOException {
        jsonWriter.flush();
    }

    public void close() throws IOException {
        jsonWriter.flush();
    }

}
