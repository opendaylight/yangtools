/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.codecs.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.codecs.TypeDefinitionAwareCodecTestHelper.getCodec;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.Uint8Codec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for Uint8CodecString.
 *
 * @author Thomas Pantelis
 */
public class Uint8CodecStringTest {

    @SuppressWarnings({ "unchecked" })
    @Test
    public void testSerialize() {
        Uint8Codec<String> codec = getCodec(BaseTypes.uint8Type(), Uint8Codec.class);

        assertEquals("serialize", "10", codec.serialize(Short.valueOf((short) 10)));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings({ "unchecked" })
    @Test
    public void testDererialize() {
        final String hexa = "0x40";
        final String octal = "0100";
        final String integer = "64";

        Uint8Codec<String> codec = getCodec(BaseTypes.uint8Type(), Uint8Codec.class);

        assertEquals("deserialize", codec.deserialize(hexa), Short.valueOf("040", 16));
        assertEquals("deserialize", codec.deserialize(octal), Short.valueOf(octal, 8));
        assertEquals("deserialize", codec.deserialize(integer), Short.valueOf(integer, 10));
        assertEquals("deserialize", codec.deserialize("0"), Short.valueOf("0", 10));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }
}
