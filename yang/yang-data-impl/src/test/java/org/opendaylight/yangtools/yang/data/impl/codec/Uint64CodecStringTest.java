/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for Uint64CodecString.
 *
 * @author Thomas Pantelis
 */
public class Uint64CodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        Uint64Codec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.uint64Type(),
            Uint64Codec.class);
        assertEquals("123456789", codec.serialize(Uint64.valueOf(123456789)));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        final String hexa = "0X75EDC78edCBA";
        final String octal = "03536670743556272";
        final String integer = "129664115727546";

        Uint64Codec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.uint64Type(),
            Uint64Codec.class);

        assertEquals(Uint64.valueOf("75EDC78edCBA", 16), codec.deserialize(hexa));
        assertEquals(Uint64.valueOf(octal, 8), codec.deserialize(octal));
        assertEquals(Uint64.valueOf(integer, 10), codec.deserialize(integer));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "12345o");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
