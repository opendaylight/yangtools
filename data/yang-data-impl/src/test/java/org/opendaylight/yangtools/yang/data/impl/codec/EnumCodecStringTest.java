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
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.toEnumTypeDefinition;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.codec.EnumCodec;

/**
 * Unit tests for EnumCodecString.
 *
 * @author Thomas Pantelis
 */
class EnumCodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    void testSerialize() {
        final var codec = getCodec(toEnumTypeDefinition("enum1", "enum2"), EnumCodec.class);
        assertEquals("enum1", codec.serialize("enum1"), "serialize");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDeserialize() {
        final var codec = getCodec(toEnumTypeDefinition("enum1", "enum2"), EnumCodec.class);

        assertEquals("enum1", codec.deserialize("enum1"), "deserialize");
        assertEquals("enum2", codec.deserialize("enum2"), "deserialize");

        deserializeWithExpectedIllegalArgEx(codec, "enum3");
    }
}
