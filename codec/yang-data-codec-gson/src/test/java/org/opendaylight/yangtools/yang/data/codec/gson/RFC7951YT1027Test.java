/*
 * Copyright (c) 2019 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.codec.gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RFC7951YT1027Test extends AbstractYT1027Test {
    private static JSONCodecFactory CODEC_FACTORY;

    @BeforeAll
    static void createFactory() {
        CODEC_FACTORY = JSONCodecFactorySupplier.RFC7951.getShared(SCHEMA_CONTEXT);
    }

    @AfterAll
    static void destroyFactory() {
        CODEC_FACTORY = null;
    }

    @Test
    void testDecimalUnquotedParsing() throws IOException {
        assertEquals(DECIMAL_DATA, fromJSON(UNQUOTED_DECIMAL));
    }

    @Test
    void testInt64UnquotedParsing() throws IOException {
        assertEquals(INT64_DATA, fromJSON(UNQUOTED_INT64));
    }

    @Test
    void testUint64UnquotedParsing() throws IOException {
        assertEquals(UINT64_DATA, fromJSON(UNQUOTED_UINT64));
    }

    @Override
    JSONCodecFactory codecFactory() {
        return CODEC_FACTORY;
    }

    @Override
    Class<?> wrapperClass() {
        return QuotedJSONCodec.class;
    }

    @Override
    String expectedDecimal() {
        return """
            {
              "yt1027:decimal": "1.1"
            }""";
    }

    @Override
    String expectedInt64() {
        return """
            {
              "yt1027:int64": "2"
            }""";
    }

    @Override
    String expectedUint64() {
        return """
            {
              "yt1027:uint64": "1"
            }""";
    }
}
