/*
 * Copyright (c) 2021 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common.jackson;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.type.ReferenceType;
import org.junit.jupiter.api.Test;
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

public class YangDeserializersTest {

    private static final YangDeserializers YANG_DESERIALIZERS = new YangDeserializers();

    @Test
    public void testFindReferenceDeserializer() throws JsonMappingException {
        assertInstanceOf(Decimal64Deserializer.class,
                YANG_DESERIALIZERS.findReferenceDeserializer(
                        ReferenceType.construct(Decimal64.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(EmptyDeserializer.class,
                YANG_DESERIALIZERS.findReferenceDeserializer(ReferenceType.construct(Empty.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(RevisionDeserializer.class,
                YANG_DESERIALIZERS.findReferenceDeserializer(ReferenceType.construct(Revision.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(Uint8Deserializer.class,
                YANG_DESERIALIZERS.findReferenceDeserializer(ReferenceType.construct(Uint8.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(Uint16Deserializer.class,
                YANG_DESERIALIZERS.findReferenceDeserializer(ReferenceType.construct(Uint16.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(Uint32Deserializer.class,
                YANG_DESERIALIZERS.findReferenceDeserializer(ReferenceType.construct(Uint32.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(Uint64Deserializer.class,
                YANG_DESERIALIZERS.findReferenceDeserializer(ReferenceType.construct(Uint64.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
        assertInstanceOf(YangVersionDeserializer.class,
                YANG_DESERIALIZERS.findReferenceDeserializer(
                        ReferenceType.construct(YangVersion.class,
                                null, null, null, null),
                        null,
                        null,
                        null,
                        null));
    }
}
