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
import static org.opendaylight.yangtools.yang.data.impl.codecs.TypeDefinitionAwareCodecTestHelper.toEnumTypeDefinition;

import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;

/**
 * Unit tests for EnumCodecString.
 *
 * @author Thomas Pantelis
 */
public class EnumCodecStringTest {

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        EnumCodec<String> codec = getCodec(toEnumTypeDefinition("enum1", "enum2"), EnumCodec.class);

        assertEquals("serialize", "enum1", codec.serialize("enum1"));
        assertEquals("serialize", "", codec.serialize(null));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        EnumCodec<String> codec = getCodec( toEnumTypeDefinition("enum1", "enum2"), EnumCodec.class);

        assertEquals("deserialize", "enum1", codec.deserialize("enum1"));
        assertEquals("deserialize", "enum2", codec.deserialize("enum2"));

        deserializeWithExpectedIllegalArgEx(codec, "enum3");
    }
}
