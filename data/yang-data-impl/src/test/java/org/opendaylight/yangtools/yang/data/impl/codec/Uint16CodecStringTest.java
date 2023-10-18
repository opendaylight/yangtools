/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.getCodec;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.data.api.codec.Uint16Codec;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Unit tests for Uint16CodecString.
 *
 * @author Thomas Pantelis
 */
class Uint16CodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    void testSerialize() {
        final var codec = getCodec(BaseTypes.uint16Type(), Uint16Codec.class);
        assertEquals("10", codec.serialize(Uint16.valueOf(10)));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDeserialize() {
        final var hexa = "0X45c";
        final var octal = "02134";
        final var integer = "1116";

        final var codec = getCodec(BaseTypes.uint16Type(), Uint16Codec.class);

        assertEquals(Uint16.valueOf("045c", 16), codec.deserialize(hexa));
        assertEquals(Uint16.valueOf(octal, 8), codec.deserialize(octal));
        assertEquals(Uint16.valueOf(integer, 10), codec.deserialize(integer));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
