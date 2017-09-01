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
import org.opendaylight.yangtools.yang.data.api.codec.Int64Codec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for Int64CodecString.
 *
 * @author Thomas Pantelis
 */
public class Int64CodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        Int64Codec<String> codec = getCodec(BaseTypes.int64Type(), Int64Codec.class);

        assertEquals("serialize", "12345", codec.serialize(Long.valueOf(12345)));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        final String hexa = "0X75EDC78edCBA";
        final String negHexa = "-0X75EDC78edCBA";
        final String octal = "+03536670743556272";
        final String negOctal = "-03536670743556272";
        final String integer = "+129664115727546";
        final String negInteger = "-129664115727546";

        Int64Codec<String> codec = getCodec(BaseTypes.int64Type(), Int64Codec.class);

        assertEquals("deserialize", codec.deserialize(hexa), Long.valueOf("075EDC78edCBA", 16));
        assertEquals("deserialize", codec.deserialize(negHexa), Long.valueOf("-075EDC78edCBA", 16));
        assertEquals("deserialize", codec.deserialize(octal), Long.valueOf(octal, 8));
        assertEquals("deserialize", codec.deserialize(negOctal), Long.valueOf(negOctal, 8));
        assertEquals("deserialize", codec.deserialize(integer), Long.valueOf(integer, 10));
        assertEquals("deserialize", codec.deserialize(negInteger), Long.valueOf(negInteger, 10));

        deserializeWithExpectedIllegalArgEx(codec, "1234o");
        deserializeWithExpectedIllegalArgEx(codec, "");
        deserializeWithExpectedIllegalArgEx(codec, null);
    }
}
