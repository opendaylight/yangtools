/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import com.google.common.annotations.Beta;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import java.io.CharArrayWriter;
import java.io.IOException;
import java.util.function.Function;
import org.opendaylight.yangtools.yang.data.api.schema.AbstractStringAnydataValue;

/**
 * Abstract base for various JSON-based representations.
 *
 * @author Robert Varga
 *
 * @param <T> AnydataValue type
 */
// FIXME: BUG-8083: we need an RFC7951 representation, too
@Beta
public abstract class JsonAnydataValue<T extends JsonAnydataValue<T>> extends AbstractStringAnydataValue<T> {
    private final String str;

    JsonAnydataValue(final String str) {
        this.str = checkNotNull(str);
    }

    /**
     * {@inheritDoc}
     *
     * Returned String complies to RFC7159.
     */
    @Override
    public final String getStringValue() {
        return str;
    }

    /**
     * Capture a {@link JsonReader} event stream for later use.
     *
     * @param in Input stream
     * @return Supplier of JsonReaders.
     * @throws IOException if an IO occurs.
     */
    static final <T extends JsonAnydataValue<T>> T create(final JsonReader in,
            final Function<String, T> constructor) throws IOException {
        final CharArrayWriter w = new CharArrayWriter();

        try (final JsonWriter out = new JsonWriter(w)) {
            int depth = 0;
            do {
                switch (in.peek()) {
                    case BEGIN_ARRAY:
                        in.beginArray();
                        out.beginArray();
                        depth++;
                        break;
                    case BEGIN_OBJECT:
                        in.beginObject();
                        out.beginObject();
                        depth++;
                        break;
                    case BOOLEAN:
                        out.value(in.nextBoolean());
                        break;
                    case END_ARRAY:
                        in.endArray();
                        out.endArray();
                        depth--;
                        break;
                    case END_DOCUMENT:
                        throw new IllegalStateException(String.format(
                            "End of documentent encountered at path %s depth=%s", in.getPath(), depth));
                    case END_OBJECT:
                        in.endObject();
                        out.endObject();
                        depth--;
                        break;
                    case NAME:
                        checkState(depth != 0, "Unsupported top-level pair");
                        out.name(in.nextName());
                        break;
                    case NULL:
                        in.nextNull();
                        out.nullValue();
                        break;
                    case NUMBER:
                        out.jsonValue(in.nextString());
                        break;
                    case STRING:
                        out.value(in.nextString());
                        break;
                    default:
                        throw new IllegalStateException("Unsupported event " + in.peek());
                }
            } while (depth != 0);
        }

        return constructor.apply(w.toString());
    }
}
