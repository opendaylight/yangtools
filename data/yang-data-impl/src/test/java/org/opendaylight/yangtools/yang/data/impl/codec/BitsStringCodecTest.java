/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.opendaylight.yangtools.yang.data.impl.codec.TypeDefinitionAwareCodecTestHelper.getCodec;

import java.util.HexFormat;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.opendaylight.yangtools.yang.common.Bits;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.ri.type.BaseTypes;
import org.opendaylight.yangtools.yang.model.ri.type.BitBuilder;
import org.opendaylight.yangtools.yang.model.ri.type.BitsTypeBuilder;

class BitsStringCodecTest {

    @ParameterizedTest(name = "Deserialize \"{1}\" -> {2}")
    @MethodSource("bitsSerializationArgs")
    void deserialization(final int bitsTotal, final String input, final Bits output) {
        final BitsCodec<String> codec = getCodec(buildBitsTypeDefinition(bitsTotal), BitsCodec.class);
        assertEquals(output, codec.deserialize(input));
    }

    @ParameterizedTest(name = "Serialize {2} -> \"{1}\"")
    @MethodSource("bitsSerializationArgs")
    void serialization(final int bitsTotal, final String output, final Bits input) {
        final BitsCodec<String> codec = getCodec(buildBitsTypeDefinition(bitsTotal), BitsCodec.class);
        assertEquals(output, codec.serialize(input));
    }

    private static Stream<Arguments> bitsSerializationArgs() {
        // (number of bits, serialized value, deserialized value)
        return Stream.of(
            Arguments.of(1, "", buildBitsObj(1, "00")),
            Arguments.of(1, buildBitsValue(0), buildBitsObj(1, "01")),
            Arguments.of(8, buildBitsValue(1, 2, 7), buildBitsObj(8, "86")),
            Arguments.of(9, buildBitsValue(0, 7, 8), buildBitsObj(9, "8101")),
            Arguments.of(64, buildBitsValue(0, 63), buildBitsObj(64, "0100000000000080"))
        );
    }

    private static BitsTypeDefinition buildBitsTypeDefinition(final int numOfBits) {
        final BitsTypeBuilder b = BaseTypes.bitsTypeBuilder(QName.create("foo", "bar"));
        IntStream.range(0, numOfBits).forEach(i -> {
            b.addBit(BitBuilder.create("Bit" + i, Uint32.valueOf(i * 2)).build());
        });
        return b.build();
    }

    private static String buildBitsValue(final int... selectedIndexes) {
        return String.join(" ", IntStream.of(selectedIndexes).mapToObj(i -> "Bit" + i).toList());
    }

    private static Bits buildBitsObj(final int numOfBits, final String bytesAsHex) {
        final Map<String, Integer> offsetMap = IntStream.range(0, numOfBits).boxed()
            .collect(Collectors.toMap(i -> "Bit" + i, i -> i));
        return Bits.of(offsetMap, HexFormat.of().parseHex(bytesAsHex));
    }
}
