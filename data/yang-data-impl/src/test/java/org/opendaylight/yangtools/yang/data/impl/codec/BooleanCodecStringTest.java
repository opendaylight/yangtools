/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Unit tests for BooleanCodecString.
 *
 * @author Thomas Pantelis
 */
class BooleanCodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    void testSerialize() {
        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.booleanType(),
            BooleanCodec.class);

        assertEquals("true", codec.serialize(Boolean.TRUE), "serialize");
        assertEquals("false", codec.serialize(Boolean.FALSE), "serialize");
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDeserialize() {
        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.booleanType(),
            BooleanCodec.class);

        assertEquals(Boolean.TRUE, codec.deserialize("true"), "deserialize");
        assertEquals(Boolean.FALSE, codec.deserialize("false"), "deserialize");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "TRUE");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "FALSE");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "foo");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
