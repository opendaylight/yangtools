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

        assertEquals("serialize", "10", codec.serialize(Long.valueOf(10)));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        final String hexa = "0x45FFFCDE";
        final String octal = "010577776336";
        final String integer = "1174404318";

        Uint32Codec<String> codec = getCodec(BaseTypes.uint32Type(), Uint32Codec.class);
        assertEquals("deserialize", codec.deserialize(hexa), Long.valueOf("45FFFCDE", 16));
        assertEquals("deserialize", codec.deserialize(octal), Long.valueOf(octal, 8));
        assertEquals("deserialize", codec.deserialize(integer), Long.valueOf(integer, 10));

        deserializeWithExpectedIllegalArgEx(codec, "1o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }
}
