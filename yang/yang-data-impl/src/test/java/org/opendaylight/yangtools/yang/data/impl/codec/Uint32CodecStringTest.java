/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.getCodec;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.codec.Uint32Codec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for Uint32CodecString.
 *
 * @author Thomas Pantelis
 */
public class Uint32CodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        Uint32Codec<String> codec = getCodec(BaseTypes.uint32Type(), Uint32Codec.class);
        assertEquals("10", codec.serialize(Uint32.valueOf(10)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        final String hexa = "0x45FFFCDE";
        final String octal = "010577776336";
        final String integer = "1174404318";

        Uint32Codec<String> codec = getCodec(BaseTypes.uint32Type(), Uint32Codec.class);
        assertEquals(Uint32.valueOf("45FFFCDE", 16), codec.deserialize(hexa));
        assertEquals(Uint32.valueOf(octal, 8), codec.deserialize(octal));
        assertEquals(Uint32.valueOf(integer, 10), codec.deserialize(integer));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
