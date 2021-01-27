/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.common.jackson.deser.Decimal64Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.EmptyDeserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.RevisionDeserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint16Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint32Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint64Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.Uint8Deserializer;
import org.opendaylight.yangtools.yang.common.jackson.deser.YangVersionDeserializer;

public class DeserializersTest {
    private static final SimpleModule DESERIALIZERS_MODULE = new SimpleModule()
            .addDeserializer(Decimal64.class, new Decimal64Deserializer())
            .addDeserializer(Empty.class, new EmptyDeserializer())
            .addDeserializer(Revision.class, new RevisionDeserializer())
            .addDeserializer(Uint8.class, new Uint8Deserializer())
            .addDeserializer(Uint16.class, new Uint16Deserializer())
            .addDeserializer(Uint32.class, new Uint32Deserializer())
            .addDeserializer(Uint64.class, new Uint64Deserializer())
            .addDeserializer(YangVersion.class, new YangVersionDeserializer());

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(DESERIALIZERS_MODULE);

    private static final String CORRECT_DECIMAL_64_STRING = "\"255.0\"";
    private static final String CORRECT_DECIMAL_64_STRING_2 = "255.0";
    private static final String EMPTY_STRING = "[null]";
    private static final String REVISION_STRING = "\"2022-10-18\"";
    private static final String UINTS_STRING = "156";
    private static final String YANG_VERSION_1_1_STRING = "\"1.1\"";
    private static final String YANG_VERSION_1_STRING = "1";

    @Test
    public void testDeserialize() throws JsonProcessingException {
        assertEquals(Decimal64.valueOf("255.0"),
                OBJECT_MAPPER.readValue(CORRECT_DECIMAL_64_STRING, Decimal64.class));
        assertEquals(Decimal64.valueOf("255.0"),
                OBJECT_MAPPER.readValue(CORRECT_DECIMAL_64_STRING_2, Decimal64.class));
        assertEquals(Empty.value(), OBJECT_MAPPER.readValue(EMPTY_STRING, Empty.class));
        assertEquals(Revision.of("2022-10-18"),
                OBJECT_MAPPER.readValue(REVISION_STRING, Revision.class));
        assertEquals(Uint8.saturatedOf(156), OBJECT_MAPPER.readValue(UINTS_STRING, Uint8.class));
        assertEquals(Uint16.saturatedOf(156), OBJECT_MAPPER.readValue(UINTS_STRING, Uint16.class));
        assertEquals(Uint32.saturatedOf(156), OBJECT_MAPPER.readValue(UINTS_STRING, Uint32.class));
        assertEquals(Uint64.saturatedOf(156), OBJECT_MAPPER.readValue(UINTS_STRING, Uint64.class));
        assertEquals(YangVersion.VERSION_1_1,
                OBJECT_MAPPER.readValue(YANG_VERSION_1_1_STRING, YangVersion.class));
        assertEquals(YangVersion.VERSION_1,
                OBJECT_MAPPER.readValue(YANG_VERSION_1_STRING, YangVersion.class));
        assertNotEquals(YangVersion.VERSION_1_1,
                OBJECT_MAPPER.readValue(YANG_VERSION_1_STRING, YangVersion.class));
        assertNotEquals(YangVersion.VERSION_1,
                OBJECT_MAPPER.readValue(YANG_VERSION_1_1_STRING, YangVersion.class));
    }

    @Test
    public void testSerializeComplex() throws JsonProcessingException {
        final var container = OBJECT_MAPPER.readValue("""
            {
                "decimal64" : "225.0",
                "empty": "[null]",
                "revision": "2022-10-18",
                "uint8": 156,
                "uint16": 156,
                "uint32":156,
                "uint64": "156",
                "yangVersion\": "1.1"
            }""", MockObjectContainer.class);
        assertEquals(Decimal64.valueOf("225.0"), container.getDecimal64());
        assertEquals(Empty.value(), container.getEmpty());
        assertEquals(Revision.of("2022-10-18"), container.getRevision());
        assertEquals(Uint8.valueOf(156), container.getUint8());
        assertEquals(Uint16.valueOf(156), container.getUint16());
        assertEquals(Uint32.valueOf(156), container.getUint32());
        assertEquals(Uint64.valueOf(156), container.getUint64());
        assertEquals(YangVersion.VERSION_1_1, container.getYangVersion());
    }
}
