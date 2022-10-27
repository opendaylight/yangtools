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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.codec.BitsNumberCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.BitsTypeBuilder;

/**
 * Unit tests for BitsCodecString.
 *
 * @author Thomas Pantelis
 */
public class BitsCodecNumberTest {
    private static final String[] BITS_TYPEDEF_LONG = {
        "bit0", "bit1", "bit2", "bit3", "bit4", "bit5", "bit6", "bit7", "bit8", "bit9", "bit10",
        "bit11", "bit12", "bit13", "bit14", "bit15", "bit16", "bit17", "bit18", "bit19", "bit20",
        "bit21", "bit22", "bit23", "bit24", "bit25", "bit26", "bit27", "bit28", "bit29", "bit30",
        "bit31", "bit32", "bit33", "bit34", "bit35", "bit36", "bit37", "bit38", "bit39", "bit40"
    };

    private static final String[] BITS_TYPEDEF_INT_ARRAY = {
        "bit0", "bit1", "bit2", "bit3", "bit4", "bit5", "bit6", "bit7", "bit8", "bit9", "bit10",
        "bit11", "bit12", "bit13", "bit14", "bit15", "bit16", "bit17", "bit18", "bit19", "bit20",
        "bit21", "bit22", "bit23", "bit24", "bit25", "bit26", "bit27", "bit28", "bit29", "bit30",
        "bit31", "bit32", "bit33", "bit34", "bit35", "bit36", "bit37", "bit38", "bit39", "bit40",
        "bit41", "bit42", "bit43", "bit44", "bit45", "bit46", "bit47", "bit48", "bit49", "bit50",
        "bit51", "bit52", "bit53", "bit54", "bit55", "bit56", "bit57", "bit58", "bit59", "bit60",
        "bit61", "bit62", "bit63", "bit64", "bit65", "bit66", "bit67", "bit68", "bit69"
    };

    private static final String PRODUCT_LONG = "bit0 bit1 bit2 bit3 bit4 bit5 bit6 bit7 bit8 bit9 bit10 "
            + "bit11 bit12 bit13 bit14 bit15 bit16 bit17 bit18 bit19 bit20 "
            + "bit21 bit22 bit23 bit24 bit25 bit26 bit27 bit28 bit29 bit30 "
            + "bit31 bit32 bit33 bit34 bit35 bit36 bit37 bit38 bit39 bit40";

    private static final String PRODUCT_INT_ARRAY = "bit0 bit1 bit2 bit3 bit4 bit5 bit6 bit7 bit8 bit9 bit10 "
            + "bit11 bit12 bit13 bit14 bit15 bit16 bit17 bit18 bit19 bit20 "
            + "bit21 bit22 bit23 bit24 bit25 bit26 bit27 bit28 bit29 bit30 "
            + "bit31 bit32 bit33 bit34 bit35 bit36 bit37 bit38 bit39 bit40 "
            + "bit41 bit42 bit43 bit44 bit45 bit46 bit47 bit48 bit49 bit50 "
            + "bit51 bit52 bit53 bit54 bit55 bit56 bit57 bit58 bit59 bit60 "
            + "bit61 bit62 bit63 bit64 bit65 bit66 bit67 bit68 bit69";

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializeInteger() {
        final BitsNumberCodec<String, Integer> codec = TypeDefinitionAwareCodecTestHelper
                .getCodec(toBitsTypeDefinition("bit0", "bit1", "bit2"), BitsNumberCodec.class);
        final String serialized = codec.serialize(7);

        assertNotNull(serialized);
        assertTrue(serialized.contains("bit0"));
        assertTrue(serialized.contains("bit1"));
        assertTrue(serialized.contains("bit2"));
        assertEquals("", codec.serialize(0));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializeLong() {
        final BitsNumberCodec<String, Long> codec = TypeDefinitionAwareCodecTestHelper
                .getCodec(toBitsTypeDefinition(BITS_TYPEDEF_LONG), BitsNumberCodec.class);

        assertEquals("serialize", PRODUCT_LONG, codec.serialize(2199023255551L));
        assertEquals("", codec.serialize(0L));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerializeIntArray() {
        final BitsNumberCodec<String, int[]> codec = TypeDefinitionAwareCodecTestHelper
                .getCodec(toBitsTypeDefinition(BITS_TYPEDEF_INT_ARRAY), BitsNumberCodec.class);

        assertEquals("serialize", PRODUCT_INT_ARRAY, codec.serialize(new int[]{-1, -1, 63}));
        assertEquals("", codec.serialize(new int[3]));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserializeInteger() {
        final BitsNumberCodec<String, Integer> codec = TypeDefinitionAwareCodecTestHelper.getCodec(
                toBitsTypeDefinition("bit1", "bit2"), BitsNumberCodec.class);

        assertEquals("deserialize", 3, (int) codec.deserialize("  bit1 bit2     "));
        assertEquals("deserialize", 0, (int) codec.deserialize(""));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "bit1 bit3");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserializeLong() {
        final BitsNumberCodec<String, Long> codec = TypeDefinitionAwareCodecTestHelper
                .getCodec(toBitsTypeDefinition(BITS_TYPEDEF_LONG), BitsNumberCodec.class);

        assertEquals("deserialize", 2199023255551L, (long) codec.deserialize(PRODUCT_LONG));
        assertEquals("deserialize", 0L, (long) codec.deserialize(""));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "bit0 bit41");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserializeIntArray() {
        final BitsNumberCodec<String, int[]> codec = TypeDefinitionAwareCodecTestHelper
                .getCodec(toBitsTypeDefinition(BITS_TYPEDEF_INT_ARRAY), BitsNumberCodec.class);

        assertArrayEquals("deserialize", new int[]{-1, -1, 63}, codec.deserialize(PRODUCT_INT_ARRAY));
        assertArrayEquals("deserialize", new int[3], codec.deserialize(""));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "bit0 bit70");
    }

    private static BitsTypeDefinition toBitsTypeDefinition(final String... bits) {
        final BitsTypeBuilder b = BaseTypes.bitsTypeBuilder(QName.create("foo", "foo"));

        long pos = 0;
        for (final String bit : bits) {
            final BitsTypeDefinition.Bit mockBit = mock(BitsTypeDefinition.Bit.class);
            doReturn(bit).when(mockBit).getName();
            doReturn(Uint32.valueOf(pos)).when(mockBit).getPosition();
            b.addBit(mockBit);
            ++pos;
        }

        return b.build();
    }
}
