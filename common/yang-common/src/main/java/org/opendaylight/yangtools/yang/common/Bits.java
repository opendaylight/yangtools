/*
 * Copyright (c) 2022 PANTHEON.tech s.r.o. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.common;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import java.io.Serial;
import java.io.Serializable;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.concepts.Immutable;

/**
 * Dedicated type for YANG's 'type bits' value.
 */
@NonNullByDefault
public final class Bits implements Immutable, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private static final Comparator<Map.Entry<String, Integer>> BY_OFFSET =
            (a, b) -> Integer.compare(a.getValue(), b.getValue());
    private static final HexFormat HEX_FORMAT = HexFormat.ofDelimiter(":").withUpperCase();
    private static final Joiner JOINER = Joiner.on(" ").skipNulls();
    private static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    private final Map<String, Integer> bitNameToOffsetMap;
    private final BitSet bits;

    private Bits(Map<String, Integer> offsetMap, BitSet bits) {
        this.bitNameToOffsetMap = offsetMap;
        this.bits = bits;
    }

    /**
     * Gets full list of bit names configured.
     *
     * @return list of bit names ordered by their position (offset value)
     */
    public List<String> getAllBitNames() {
        return bitNameToOffsetMap.entrySet().stream().sorted(BY_OFFSET).map(Map.Entry::getKey).toList();
    }

    /**
     * Provides bit state by name.
     *
     * @param bitName requested bit name
     * @return true if bit is set, false otherwise
     * @throws NullPointerException     if bitName is null
     * @throws IllegalArgumentException if bitName value does not match any of configured bit names
     */
    public boolean isBitSet(final @NonNull String bitName) {
        validateBitName(bitNameToOffsetMap, bitName);
        return bits.get(bitNameToOffsetMap.get(bitName));
    }

    /**
     * Represents value as collection of selected bit names.
     *
     * @return set of bit names
     */
    public Set<String> toSet() {
        return ImmutableSet.copyOf(getSelectedBitNames());
    }

    /**
     * Represents value as binary data.
     *
     * @return byte array
     */
    public byte[] toByteArray() {
        return bits.toByteArray();
    }

    /**
     * Represents value as canonically formatted:
     * selected bit names separated by single space ordered by their position.
     *
     * @return canonically formatted value
     */
    public String toStringValue() {
        return JOINER.join(getSelectedBitNames());
    }

    private List<String> getSelectedBitNames() {
        return bitNameToOffsetMap.entrySet().stream().sorted(BY_OFFSET)
                .filter(entry -> bits.get(entry.getValue()))
                .map(Map.Entry::getKey).toList();
    }

    /**
     * Builds an instance.
     *
     * @param offsetMap bit name to offset mapping, offset value represents actual bit position
     * @param value     the value as canonically formatted string
     * @return instance
     * @throws NullPointerException     if either offsetMap or value is null
     * @throws IllegalArgumentException if a) offsetMap is empty or b) offsetMap contains offset value greater or
     *                                  equal to total number of bits or c) value contains bit name not listed
     *                                  within offsetMap
     */
    public static Bits of(final @NonNull Map<String, Integer> offsetMap, final @NonNull String value) {
        return of(offsetMap, SPLITTER.split(requireNonNull(value)));
    }

    /**
     * Builds an instance.
     *
     * @param offsetMap        bit name to offset mapping, offset value represents actual bit position
     * @param selectedBitNames selected bit names
     * @return instance
     * @throws NullPointerException     if either offsetMap or selectedBitNames is null
     * @throws IllegalArgumentException if a) offsetMap is empty or b) offsetMap contains offset value greater or
     *                                  equal to total number of bits or c) selectedBitNames contains bit name
     *                                  not listed within offsetMap
     */
    public static Bits of(final @NonNull Map<String, Integer> offsetMap,
            final @NonNull Iterable<String> selectedBitNames) {
        validateOffsetMap(offsetMap);
        requireNonNull(selectedBitNames);
        selectedBitNames.forEach(bitName -> validateBitName(offsetMap, bitName));

        final BitSet bitSet = new BitSet();
        selectedBitNames.forEach(bitName -> bitSet.set(offsetMap.get(bitName)));
        return new Bits(offsetMap, bitSet);
    }

    /**
     * Builds an instance.
     *
     * @param offsetMap bit name to offset mapping, offset value represents actual bit position
     * @param bytes     bits in a binary format
     * @return instance
     * @throws NullPointerException     if either offsetMap or selectedBitNames is null
     * @throws IllegalArgumentException if a) offsetMap is empty or b) offsetMap contains offset value greater or
     *                                  equal to total number of bits
     */
    public static Bits of(final @NonNull Map<String, Integer> offsetMap, final @NonNull byte[] bytes) {
        validateOffsetMap(offsetMap);
        requireNonNull(bytes);
        return new Bits(offsetMap, BitSet.valueOf(bytes));
    }

    private static void validateOffsetMap(final Map<String, Integer> offsetMap) {
        requireNonNull(offsetMap);
        checkArgument(!offsetMap.isEmpty(), "Offset map should not be empty");
        final int maxOffset = offsetMap.size() - 1;
        final List<Integer> invalidOffsets = offsetMap.values().stream().filter(i -> i > maxOffset).toList();
        checkArgument(invalidOffsets.isEmpty(), "Offset map contains invalid values %s, max offset is %s",
                invalidOffsets, maxOffset);
    }

    private static void validateBitName(final Map<String, Integer> offsetMap, final String bitName) {
        requireNonNull(bitName);
        checkArgument(offsetMap.containsKey(bitName), "Unknown bit name %s", bitName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bits bits = (Bits) o;
        return bitNameToOffsetMap.equals(bits.bitNameToOffsetMap) && this.bits.equals(bits.bits);
    }

    @Override
    public int hashCode() {
        return Objects.hash(bitNameToOffsetMap, bits);
    }

    @Override
    public String toString() {
        return "Bits" + getSelectedBitNames() + "(" + HEX_FORMAT.formatHex(toByteArray()) + ")";
    }
}
