/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codecs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import org.junit.Test;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.util.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.util.type.BitsTypeBuilder;

/**
 * Unit tests for BitsCodecString.
 *
 * @author Thomas Pantelis
 *
 */
public class BitsCodecStringTest {
    private  static BitsTypeDefinition toBitsTypeDefinition(final String... bits) {
        final BitsTypeBuilder b = BaseTypes.bitsTypeBuilder(mock(SchemaPath.class));

        long pos = 0;
        for (String bit : bits) {
            BitsTypeDefinition.Bit mockBit = mock(BitsTypeDefinition.Bit.class);
            when(mockBit.getName()).thenReturn(bit);
            when(mockBit.getPosition()).thenReturn(Uint32.valueOf(pos));
            b.addBit(mockBit);
            ++pos;
        }

        return b.build();
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSerialize() {
        BitsCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(toBitsTypeDefinition("foo"),
                BitsCodec.class);

        String serialized = codec.serialize(ImmutableSet.of("foo", "bar"));
        assertNotNull(serialized);
        assertTrue(serialized.contains("foo"));
        assertTrue(serialized.contains("bar"));

        assertEquals("", codec.serialize(ImmutableSet.of()));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDeserialize() {
        BitsCodec<String> codec = TypeDefinitionAwareCodecTestHelper.getCodec(
            toBitsTypeDefinition("bit1", "bit2"), BitsCodec.class);

        assertEquals("deserialize", ImmutableSet.of("bit1", "bit2"), codec.deserialize("  bit1 bit2     "));
        assertEquals("deserialize", Collections.emptySet(), codec.deserialize(""));

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "bit1 bit3");
    }
}
