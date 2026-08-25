/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.getCodec;

import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Unit tests for BooleanCodecString.
 *
 * @author Thomas Pantelis
 */
class BooleanCodecStringTest {
    @Test
    @SuppressWarnings("unchecked")
    void testSerialize() {
        final var codec = getCodec(BaseTypes.booleanType(), BooleanCodec.class);
        assertEquals("true", codec.serialize(true), "serialize");
        assertEquals("false", codec.serialize(false), "serialize");
    }

    @Test
    @SuppressWarnings("unchecked")
    void testDeserialize() {
        final var codec = getCodec(BaseTypes.booleanType(), BooleanCodec.class);
        assertTrue(assertInstanceOf(Boolean.class, codec.deserialize("true")));
        assertFalse(assertInstanceOf(Boolean.class, codec.deserialize("false")));
        deserializeWithExpectedIllegalArgEx(codec, "TRUE");
        deserializeWithExpectedIllegalArgEx(codec, "FALSE");
        deserializeWithExpectedIllegalArgEx(codec, "foo");
        deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
