/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.BitSet;
import java.util.Comparator;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.codec.BitsCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition.Bit;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class BitsStringCodec extends TypeDefinitionAwareCodec<String, BitsTypeDefinition>
        implements BitsCodec<String> {

    private static final Comparator<Bit> BIT_COMPARATOR = Comparator.comparing(Bit::getPosition);
    private static final Joiner JOINER = Joiner.on(" ").skipNulls();
    private static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static final HexFormat HEX_FORMAT = HexFormat.of().withUpperCase();

    private final ImmutableMap<String, Integer> bitNameToIndexMap;
    private final int numberOfBits;

    private BitsStringCodec(final @Nullable BitsTypeDefinition typeDef) {
        super(requireNonNull(typeDef), String.class);
        final AtomicInteger counter = new AtomicInteger(0);
        bitNameToIndexMap = typeDef.getBits().stream().sorted(BIT_COMPARATOR)
                .collect(ImmutableMap.toImmutableMap(Bit::getName, bit -> counter.getAndIncrement()));
        numberOfBits = bitNameToIndexMap.size();
    }

    public static BitsStringCodec from(final BitsTypeDefinition type) {
        return new BitsStringCodec(type);
    }

    @Override
    protected @NonNull String deserializeImpl(final @NonNull String input) {
        final Set<String> bitNames = ImmutableSet.copyOf(SPLITTER.split(input));
        final Set<String> unknownBitNames = bitNames.stream()
                .filter(bitName -> !bitNameToIndexMap.containsKey(bitName))
                .collect(ImmutableSet.toImmutableSet());
        checkArgument(unknownBitNames.isEmpty(), "Unknown bit name(s) provided: %s, eligible values are: %s",
                unknownBitNames, bitNameToIndexMap.keySet());

        final BitSet bitSet = new BitSet(numberOfBits);
        bitNames.forEach(bitName -> bitSet.set(bitNameToIndexMap.get(bitName)));
        return HEX_FORMAT.formatHex(bitSet.toByteArray());
    }

    @Override
    protected @NonNull String serializeImpl(final @NonNull String input) {
        final byte[] bytes = HEX_FORMAT.parseHex(input);
        final BitSet bitSet = BitSet.valueOf(bytes);

        final List<String> selectedBitNames = bitNameToIndexMap.entrySet().stream()
                .filter(entry -> bitSet.get(entry.getValue())).map(Map.Entry::getKey).toList();
        return JOINER.join(selectedBitNames);
    }
}