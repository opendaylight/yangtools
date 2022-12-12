/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. and others. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.common;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class BitsTest {

    private static final Class<?> NPE = NullPointerException.class;
    private static final Class<?> IAE = IllegalArgumentException.class;
    private static final Map<String, Integer> OFFSET_MAP_VALID = Map.of("Bit0", 0, "Bit1", 2, "Bit2", 2);
    private static final Map<String, Integer> OFFSET_MAP_INVALID = Map.of("Bit0", 0, "Bit1", 3, "Bit2", 5);
    private static final String STR_VALUE_VALID = "Bit0 Bit1";
    private static final String STR_VALUE_INVALID = "Bit0 Bit5";
    private static final Set<String> SET_VALUE_INVALID = Set.of("Bit0", "Bit5");

    @ParameterizedTest(name = "Bits instantiation: total={0}, selected indexes={1}")
    @MethodSource("bitsInstantiationArgs")
    void bitsInstantiation(final int numberOfBits, final List<Integer> selectedBitOffsets) {

        // generate data
        final Map<String, Integer> offsetMap = IntStream.range(0, numberOfBits).boxed()
            .collect(Collectors.toMap(BitsTest::bitName, i -> i));
        final Map<String, Boolean> valueMap = IntStream.range(0, numberOfBits).boxed()
            .collect(Collectors.toMap(BitsTest::bitName, selectedBitOffsets::contains));
        final List<String> allBitNames = IntStream.range(0, numberOfBits).boxed().map(BitsTest::bitName).toList();
        final List<String> selectedBitNames = selectedBitOffsets.stream().map(BitsTest::bitName).toList();

        final String expectedStringValue = String.join(" ", selectedBitNames);
        final Set<String> expectedSetValue = Set.copyOf(selectedBitNames);
        final byte[] expectedBytes = getBytes(selectedBitOffsets);

        // instantiate using canonically formatted value
        final Bits bits1 = Bits.of(offsetMap, expectedStringValue);
        assertNotNull(bits1);
        assertEquals(allBitNames, bits1.getAllBitNames());
        assertEquals(expectedStringValue, bits1.toStringValue());
        assertEquals(expectedSetValue, bits1.toSet());
        assertArrayEquals(expectedBytes, bits1.toByteArray());
        valueMap.forEach((key, value) -> assertEquals(value, bits1.isBitSet(key)));

        // instantiate using collection of selected bit names
        final Bits bits2 = Bits.of(offsetMap, selectedBitNames);
        assertNotNull(bits2);
        assertEquals(allBitNames, bits2.getAllBitNames());
        assertEquals(expectedStringValue, bits2.toStringValue());
        assertEquals(expectedSetValue, bits2.toSet());
        assertArrayEquals(expectedBytes, bits2.toByteArray());
        valueMap.forEach((key, value) -> assertEquals(value, bits2.isBitSet(key)));

        // instantiate using byte array
        final Bits bits3 = Bits.of(offsetMap, expectedBytes);
        assertNotNull(bits3);
        assertEquals(allBitNames, bits3.getAllBitNames());
        assertEquals(expectedStringValue, bits3.toStringValue());
        assertEquals(expectedSetValue, bits3.toSet());
        assertArrayEquals(expectedBytes, bits3.toByteArray());
        valueMap.forEach((key, value) -> assertEquals(value, bits3.isBitSet(key)));

        // all instances represent same data
        assertEquals(bits1, bits2);
        assertEquals(bits2, bits3);
    }

    private static Stream<Arguments> bitsInstantiationArgs() {
        // (number of bits, selected indexes)
        return Stream.of(
            Arguments.of(1, List.of()),
            Arguments.of(1, List.of(0)),
            Arguments.of(8, List.of(1, 2, 7)),
            Arguments.of(9, List.of(0, 7, 8)),
            Arguments.of(64, List.of(0, 63))
        );
    }

    @ParameterizedTest(name = "Bits instantiation failure: {0}")
    @MethodSource("bitsInstantiationFailureArgs")
    void bitsInstantiationFailure(final String testDesc, final Class<? extends Exception> exceptionClass,
        final Callable<Bits> instanceBuilder) {
        assertThrows(exceptionClass, instanceBuilder::call);
    }

    private static Stream<Arguments> bitsInstantiationFailureArgs() {
        // (test case descriptor, expected exception thrown, Bits instance builder)
        return Stream.of(
            Arguments.of("offset map is null", NPE,
                (Callable<Bits>) () -> Bits.of(null, STR_VALUE_VALID)),
            Arguments.of("offsetMap is empty", IAE,
                (Callable<Bits>) () -> Bits.of(Map.of(), STR_VALUE_VALID)),
            Arguments.of("offsetMap is invalid", IAE,
                (Callable<Bits>) () -> Bits.of(OFFSET_MAP_INVALID, STR_VALUE_VALID)),
            Arguments.of("string value is null", NPE,
                (Callable<Bits>) () -> Bits.of(OFFSET_MAP_VALID, (String) null)),
            Arguments.of("string value is invalid", IAE,
                (Callable<Bits>) () -> Bits.of(OFFSET_MAP_VALID, STR_VALUE_INVALID)),
            Arguments.of("set value is null", NPE,
                (Callable<Bits>) () -> Bits.of(OFFSET_MAP_VALID, (Set<String>) null)),
            Arguments.of("set value is invalid", IAE,
                (Callable<Bits>) () -> Bits.of(OFFSET_MAP_VALID, SET_VALUE_INVALID)),
            Arguments.of("bytes value is null", NPE,
                (Callable<Bits>) () -> Bits.of(OFFSET_MAP_VALID, (byte[]) null))
        );
    }

    private static byte[] getBytes(final List<Integer> selectedBitOffsets) {
        final BitSet bitSet = new BitSet();
        selectedBitOffsets.forEach(bitSet::set);
        return bitSet.toByteArray();
    }

    private static String bitName(final int offset) {
        return "Bit" + offset;
    }
}
