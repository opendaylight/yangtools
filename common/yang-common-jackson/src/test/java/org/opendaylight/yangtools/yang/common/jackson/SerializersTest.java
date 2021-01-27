/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import java.util.Optional;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.Empty;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.common.YangVersion;
import org.opendaylight.yangtools.yang.common.jackson.ser.Decimal64Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.EmptySerializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.RevisionSerializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint16Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint32Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint64Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.Uint8Serializer;
import org.opendaylight.yangtools.yang.common.jackson.ser.YangVersionSerializer;

public class SerializersTest {

    private static final SimpleModule SERIALIZERS_MODULE = new SimpleModule()
            .addSerializer(Decimal64.class, new Decimal64Serializer())
            .addSerializer(Empty.class, new EmptySerializer())
            .addSerializer(Revision.class, new RevisionSerializer())
            .addSerializer(Uint8.class, new Uint8Serializer())
            .addSerializer(Uint16.class, new Uint16Serializer())
            .addSerializer(Uint32.class, new Uint32Serializer())
            .addSerializer(Uint64.class, new Uint64Serializer())
            .addSerializer(YangVersion.class, new YangVersionSerializer());
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(
            SERIALIZERS_MODULE);

    private static final Decimal64 CORRECT_DECIMAL_64 = Decimal64.valueOf("125");
    private static final Empty EMPTY = Empty.value();
    private static final Revision REVISION = Revision.of("2022-10-18");
    private static final Optional<Revision> NON_EMPTY_REVISION = Revision.ofNullable("2022-10-18");
    private static final Optional<Revision> EMPTY_REVISION = Revision.ofNullable(null);
    private static final Uint8 UINT_8 = Uint8.saturatedOf(168);
    private static final Uint16 UINT_16 = Uint16.saturatedOf(168);
    private static final Uint32 UINT_32 = Uint32.saturatedOf(168);
    private static final Uint64 UINT_64 = Uint64.saturatedOf(168);
    private static final YangVersion YANG_VERSION = YangVersion.VERSION_1_1;

    private static final MockObjectContainer MOCK_OBJECT_CONTAINER = new MockObjectContainer();
    private static final String COMPLEX_SERIALIZATION_STRING =
            "{\"decimal64\":\"125.0\",\"empty\":\"[null]\",\"revision\":\"2022-10-18\","
                    + "\"uint8\":168,\"uint16\":168,\"uint32\":168,\"uint64\":\"168\",\"yangVersion\":\"1.1\"}";

    @Test
    public void testSerialize() throws JsonProcessingException {
        assertEquals("\"125.0\"", OBJECT_MAPPER.writeValueAsString(CORRECT_DECIMAL_64));
        assertEquals("\"[null]\"", OBJECT_MAPPER.writeValueAsString(EMPTY));
        assertEquals("\"2022-10-18\"", OBJECT_MAPPER.writeValueAsString(REVISION));
        assertEquals("\"2022-10-18\"", OBJECT_MAPPER.writeValueAsString(NON_EMPTY_REVISION.get()));
        assertThrows(java.util.NoSuchElementException.class,
                () -> OBJECT_MAPPER.writeValueAsString(EMPTY_REVISION.get()));
        assertEquals("168", OBJECT_MAPPER.writeValueAsString(UINT_8));
        assertEquals("168", OBJECT_MAPPER.writeValueAsString(UINT_16));
        assertEquals("168", OBJECT_MAPPER.writeValueAsString(UINT_32));
        assertEquals("\"168\"", OBJECT_MAPPER.writeValueAsString(UINT_64));
        assertEquals("\"1.1\"", OBJECT_MAPPER.writeValueAsString(YANG_VERSION));
    }

    @Test
    public void testSerializeComplex() throws JsonProcessingException {
        MOCK_OBJECT_CONTAINER.setDecimal64(CORRECT_DECIMAL_64);
        MOCK_OBJECT_CONTAINER.setEmpty(EMPTY);
        MOCK_OBJECT_CONTAINER.setRevision(REVISION);
        MOCK_OBJECT_CONTAINER.setUint8(UINT_8);
        MOCK_OBJECT_CONTAINER.setUint16(UINT_16);
        MOCK_OBJECT_CONTAINER.setUint32(UINT_32);
        MOCK_OBJECT_CONTAINER.setUint64(UINT_64);
        MOCK_OBJECT_CONTAINER.setYangVersion(YANG_VERSION);
        assertEquals(COMPLEX_SERIALIZATION_STRING,
                OBJECT_MAPPER.writeValueAsString(MOCK_OBJECT_CONTAINER));
    }

}
