/**
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import java.io.Writer;

/**
 * Factory Method class for JSONWriter creation
 */
public class JSONWriterFactory {

    public static enum JSONWriterType {
        DEFAULT_JSON_WRITER, GOOGLE_JSON_WRITER
    }

    public static JSONWriter createJSONWriter(Writer writer) {
        return createJSONWriter(writer, JSONWriterType.DEFAULT_JSON_WRITER);
    }

    public static JSONWriter createJSONWriter(Writer writer, JSONWriterType jsonWriterType) {
        JSONWriter jsonWriter = null;

        switch (jsonWriterType) {
        case GOOGLE_JSON_WRITER:
            jsonWriter = new JSONWriterGsonAdapter(writer);
            break;
        /*
         * case CUSTOM_JSON_WRITER_1:
         *     jsonWriter = new CustomJSONWriterAdapter_1(writer);
         *     break;
         */
        case DEFAULT_JSON_WRITER:
            jsonWriter = new JSONWriterGsonAdapter(writer);
            break;
        default:
            throw new IllegalArgumentException("Unsupported JSONWriterType");
        }

        return jsonWriter;
    }

}
