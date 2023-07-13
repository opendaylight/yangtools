/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;

/**
 * Unit tests for BitsCodecString.
 *
 * @author Thomas Pantelis
 */
class BitsCodecStringTest {
    private  static BitsTypeDefinition toBitsTypeDefinition(final String... bits) {
        final var b = BaseTypes.bitsTypeBuilder(QName.create("foo", "foo"));

        long pos = 0;
        for (var bit : bits) {
            final var mockBit = mock(Bit.class);
            doReturn(bit).when(mockBit).getName();
            doReturn(Uint32.valueOf(pos)).when(mockBit).getPosition();
            b.addBit(mockBit);
            ++pos;
        }

        return b.build();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testSerialize() {
        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(toBitsTypeDefinition("foo"),
                BitsCodec.class);

        final var serialized = ((BitsCodec<String>) codec).serialize(ImmutableSet.of("foo", "bar"));
        assertEquals("foo bar", serialized);

        assertEquals("", codec.serialize(Set.of()));
    }

    @SuppressWarnings("unchecked")
    @Test
    void testDeserialize() {
        final var codec = TypeDefinitionAwareCodecTestHelper.getCodec(
            toBitsTypeDefinition("bit1", "bit2"), BitsCodec.class);

        assertEquals(Set.of("bit1", "bit2"), codec.deserialize("  bit1 bit2     "), "deserialize");
        assertEquals(Set.of(), codec.deserialize(""), "deserialize");

        TypeDefinitionAwareCodecTestHelper.deserializeWithExpectedIllegalArgEx(codec, "bit1 bit3");
    }
}
