/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.getCodec;

import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.BitsTypeBuilder;

public class BitsCodecStringTest {

    @ParameterizedTest(name = "Deserialize \"{1}\" -> \"{2}\"")
    @MethodSource("bitsSerializationArgs")
    void deserialization(int bitsTotal, final String input, final String output) {
        final BitsCodec<String> codec = getCodec(buildBitsTypeDefinition(bitsTotal), BitsCodec.class);
        assertEquals(output, codec.deserialize(input));
    }

    @ParameterizedTest(name = "Serialize \"{2}\" -> \"{1}\"")
    @MethodSource("bitsSerializationArgs")
    void serialization(int bitsTotal, final String output, final String input) {
        final BitsCodec<String> codec = getCodec(buildBitsTypeDefinition(bitsTotal), BitsCodec.class);
        assertStringSetsAreEqual(output, codec.serialize(input));
    }

    private static Stream<Arguments> bitsSerializationArgs() {
        // (number of bits, serialized value, deserialized value)
        return Stream.of(
                Arguments.of(1, "", ""),
                Arguments.of(1, toBitNames(0), "01"),
                Arguments.of(8, toBitNames(1, 2, 7), "86"),
                Arguments.of(9, toBitNames(0, 7, 8), "8101"),
                Arguments.of(64, toBitNames(0, 63), "0100000000000080")
        );
    }

    @Test
    void deserializationFailure() {
        final BitsCodec<String> codec = getCodec(buildBitsTypeDefinition(8), BitsCodec.class);
        assertThrows(IllegalArgumentException.class,
                () -> codec.deserialize("Bit1 Bit10")); // non-existing bit name Bit10
    }

    @Test
    void serializationFailure() {
        final BitsCodec<String> codec = getCodec(buildBitsTypeDefinition(1), BitsCodec.class);
        assertThrows(IllegalArgumentException.class, () -> codec.serialize("non-hex input"));
    }

    private static BitsTypeDefinition buildBitsTypeDefinition(final int numOfBits) {
        final BitsTypeBuilder b = BaseTypes.bitsTypeBuilder(QName.create("foo", "bar"));
        IntStream.range(0, numOfBits).forEach(i -> {
            b.addBit(BitBuilder.create("Bit" + i, Uint32.valueOf(i * 2)).build());
        });
        return b.build();
    }

    private static String toBitNames(int... selectedIndexes) {
        return String.join(" ", IntStream.of(selectedIndexes).mapToObj(i -> "Bit" + i).toList());
    }

    private static void assertStringSetsAreEqual(final String expected, final String actual) {
        final Set<String> expectedSet = Set.of(expected.trim().split("\\s+"));
        final Set<String> actualSet = actual == null ? null : Set.of(actual.trim().split("\\s+"));
        assertEquals(expectedSet, actualSet);
    }
}
