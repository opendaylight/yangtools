/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.getCodec;

import java.util.Base64;
import org.junit.Test;
import org.opendaylight.yangtools.yang.data.api.codec.BinaryCodec;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;

/**
 * Unit tests for BinaryCodecString.
 *
 * @author Thomas Pantelis
 */
public class BinaryCodecStringTest {
    private static final byte[] FOUR_ELEMENTS = { 1, 2, 3, 4 };

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        BinaryCodec<String> codec = getCodec(BaseTypes.binaryType(), BinaryCodec.class);
        assertEquals(Base64.getEncoder().encodeToString(FOUR_ELEMENTS), codec.serialize(FOUR_ELEMENTS));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDererialize() {
        BinaryCodec<String> codec = getCodec(BaseTypes.binaryType(), BinaryCodec.class);
        assertArrayEquals(FOUR_ELEMENTS, codec.deserialize(Base64.getEncoder().encodeToString(FOUR_ELEMENTS)));
    }
}
