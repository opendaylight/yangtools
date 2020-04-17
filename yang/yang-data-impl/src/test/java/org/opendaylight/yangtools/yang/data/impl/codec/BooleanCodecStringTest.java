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
import org.opendaylight.yangtools.yang.data.api.codec.BooleanCodec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for BooleanCodecString.
 *
 * @author Thomas Pantelis
 */
public class BooleanCodecStringTest {
    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        BooleanCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.booleanType(),
            BooleanCodec.class);

        assertEquals("serialize", "true", codec.serialize(Boolean.TRUE));
        assertEquals("serialize", "false", codec.serialize(Boolean.FALSE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        BooleanCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(BaseTypes.booleanType(),
            BooleanCodec.class);

        assertEquals("deserialize", Boolean.TRUE, codec.deserialize("true"));
        assertEquals("deserialize", Boolean.FALSE, codec.deserialize("false"));
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "TRUE");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "FALSE");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "foo");
        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "");
    }
}
