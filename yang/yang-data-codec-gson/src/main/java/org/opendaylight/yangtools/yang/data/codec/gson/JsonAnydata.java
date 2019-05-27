/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Verify.verifyNotNull;
import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects.ToStringHelper;
import com.google.common.collect.ImmutableList;
import com.google.gson.internal.bind.JsonTreeReader;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizedNodeStreamWriter;
import org.opendaylight.yangtools.yang.data.impl.schema.AbstractNormalizableAnydata;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextNode;
import org.opendaylight.yangtools.yang.data.util.DataSchemaContextTree;
import org.opendaylight.yangtools.yang.model.api.DataSchemaNode;
import org.opendaylight.yangtools.yang.model.api.SchemaContext;

/**
 * Internal representation of JSON anydata. We are not using JsonElement, as its support classes are in gson.internal
 * and are hence completely unusable in OSGi environment. Furthermore our internal representation is more concise,
 * hopefully improving memory overhead.
 *
 * @author Robert Varga
 */
@NonNullByDefault
final class JsonAnydata extends AbstractNormalizableAnydata {
    private static final Object JSON_NULL = new Object();
    private static final Object[] EMPTY_LIST = new Object[0];

    private final JSONCodecFactory codecs;
    private final Object element;

    private JsonAnydata(final JSONCodecFactory codecs, final Object element) {
        this.codecs = requireNonNull(codecs);
        this.element = requireNonNull(element);
    }

    static JsonAnydata readFrom(final JsonReader reader, final JSONCodecFactory codecs) throws IOException {
        return new JsonAnydata(codecs, read(reader));
    }

    void writeTo(final JsonWriter writer) throws IOException {
        write(writer, element);
    }

    @Override
    protected void writeTo(final NormalizedNodeStreamWriter streamWriter, final DataSchemaContextTree contextTree,
            final DataSchemaContextNode<?> contextNode) throws IOException {
        // TODO: this is rather ugly
        final DataSchemaNode root = contextTree.getRoot().getDataSchemaNode();
        if (!(root instanceof SchemaContext)) {
            throw new IOException("Unexpected root context " + root);
        }

        final JSONCodecFactory factory = codecs.rebaseTo((SchemaContext) root);
        final JsonParserStream jsonParser;
        try {
            jsonParser = JsonParserStream.create(streamWriter, factory, verifyNotNull(contextNode.getDataSchemaNode()));
        } catch (IllegalArgumentException e) {
            throw new IOException("Failed to instantiate XML parser", e);
        }
        jsonParser.parse(new JsonTreeReader(element)).flush();
    }

    @Override
    protected ToStringHelper addToStringAttributes(final ToStringHelper helper) {
        return helper.add("element", element);
    }

    private static Object read(final JsonReader reader) throws IOException {
        final JsonToken next = reader.peek();
        switch (next) {
            case NULL:
                reader.nextNull();
                return JSON_NULL;
            case NUMBER:
                return new JSONNumber(reader.nextString());
            case BOOLEAN:
                return reader.nextBoolean();
            case STRING:
                return verifyNotNull(reader.nextString());
            case BEGIN_ARRAY:
                return readArray(reader);
            case BEGIN_OBJECT:
                return readObject(reader);
            default:
                throw new IllegalStateException("Unhandled token " + next);
        }
    }

    private static Object[] readArray(final JsonReader reader) throws IOException {
        reader.beginArray();
        final List<Object> ret = new ArrayList<>();
        while (reader.hasNext()) {
            ret.add(read(reader));
        }
        reader.endArray();
        return ret.isEmpty() ? EMPTY_LIST : ret.toArray();
    }

    private static List<?> readObject(final JsonReader reader) throws IOException {
        reader.beginObject();
        final Map<String, Object> tmp = new LinkedHashMap<>();
        while (reader.hasNext()) {
            tmp.put(verifyNotNull(reader.nextName()), read(reader));
        }
        reader.endObject();
        if (tmp.isEmpty()) {
            return ImmutableList.of();
        }

        final List<Object> ret = new ArrayList<>(tmp.size() * 2);
        for (Entry<String, Object> e : tmp.entrySet()) {
            ret.add(e.getKey());
            ret.add(e.getValue());
        }
        return ret;
    }

    private static void write(final JsonWriter writer, final @Nullable Object obj) throws IOException {
        if (obj == JSON_NULL) {
            writer.nullValue();
        } else if (obj instanceof Boolean) {
            writer.value(((Boolean) obj).booleanValue());
        } else if (obj instanceof String) {
            writer.value((String) obj);
        } else if (obj instanceof JSONNumber) {
            writer.value((JSONNumber) obj);
        } else if (obj instanceof List) {
            write(writer, (List<Entry<String, ?>>) obj);
        } else if (obj instanceof Object[]) {
            write(writer, (Object[]) obj);
        } else {
            throw new IllegalStateException("Unhandled object " + obj);
        }
    }

    private static void write(final JsonWriter writer, final Object[] obj) throws IOException {
        writer.beginArray();
        for (Object e : obj) {
            write(writer, e);
        }
        writer.endArray();
    }

    private static void write(final JsonWriter writer, final List<Entry<String, ?>> obj) throws IOException {
        writer.beginObject();
        for (Entry<String, ?> e : obj) {
            writer.name(e.getKey());
            write(writer, e.getValue());
        }
        writer.endObject();
    }

    // Fake Number implementation which retains the underlying string
    private static final class JSONNumber extends Number {
        private static final long serialVersionUID = 1L;

        private final String string;

        JSONNumber(final String string) {
            this.string = requireNonNull(string);
        }

        @Override
        public String toString() {
            return string;
        }

        @Override
        public int intValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long longValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public float floatValue() {
            throw new UnsupportedOperationException();
        }

        @Override
        public double doubleValue() {
            throw new UnsupportedOperationException();
        }
    }

    private static final class ObjectTreeReader extends JsonReader {
        private static final Reader DUMMY_READER = new Reader() {

            @Override
            public int read(final char @Nullable [] cbuf, final int off, final int len) throws IOException {
                throw new UnsupportedOperationException();
            }

            @Override
            public void close() {
                throw new UnsupportedOperationException();
            }
        };

        private final Deque<Object> stack = new ArrayDeque<>();

        ObjectTreeReader(final Object obj) {
            super(DUMMY_READER);
            stack.push(obj);
        }

        @Override
        public JsonToken peek() throws IOException {

        }
    }
}
