/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Uint32;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

public abstract class AbstractBitsNumberCodec<N> extends TypeDefinitionAwareCodec<N, BitsTypeDefinition> {

    private static final Joiner JOINER = Joiner.on(" ").skipNulls();

    private static final Splitter SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();

    private final ImmutableMap<String, Uint32> mapOfValidBits;

    protected AbstractBitsNumberCodec(@Nullable final BitsTypeDefinition typeDef, final Class<N> outputClass) {
        super(requireNonNull(typeDef), outputClass);
        mapOfValidBits = typeDef.getBits().stream()
                .collect(ImmutableMap.toImmutableMap(BitsTypeDefinition.Bit::getName, BitsTypeDefinition.Bit::getPosition));
    }

    public static AbstractBitsNumberCodec<?> from(final BitsTypeDefinition type) {
        final int size = type.getBits().size();
        if (size < 32) {
            return new BitsIntegerCodec(type, Integer.class);
        } else if (size < 64) {
            return new BitsLongCodec(type, Long.class);
        }
        return new BitsIntArrayCodec(type, int[].class);
    }

    @Override
    protected @NonNull N deserializeImpl(@NonNull final String product) {
        final var strings = ImmutableSet.copyOf(SPLITTER.split(product));
        final var sortedBits = ImmutableList.copyOf(mapOfValidBits.keySet());

        final N allSetBits = setAllBits(sortedBits.size());
        final N currentBits = setCurrentBits(strings, sortedBits);
        final int lengthOfBits = getLengthOfBits(currentBits);

        if (lengthOfBits != strings.size()) {
            for (final var bit : strings) {
                checkArgument(sortedBits.contains(bit),
                        "Invalid value '%s' for bits type. Allowed values are: %s", bit, sortedBits);
            }
        }
        return lengthOfBits == sortedBits.size() ? allSetBits : currentBits;
    }

    @Override
    protected @NonNull String serializeImpl(@NonNull final N input) {
        final var strings = new LinkedHashSet<String>();
        final var sortedBits = ImmutableList.copyOf(mapOfValidBits.keySet());

        for (final var bit : sortedBits) {
            final boolean isBitSet = isBitSet(sortedBits.indexOf(bit), input);
            if (isBitSet) {
                strings.add(bit);
            }
        }
        return JOINER.join(strings);
    }

    protected static void validatePosition(final int position) {
        if (position < 0) {
            throw new IndexOutOfBoundsException("Position < 0: " + position);
        }
    }

    protected abstract @NonNull N setCurrentBits(Set<String> strings, List<String> validBits);

    protected abstract @NonNull N setBit(N bits, boolean value, int position);

    protected abstract @NonNull N setAllBits(int sizeOfBits);

    protected abstract boolean isBitSet(int position, N bits);

    protected abstract int getLengthOfBits(N bits);

}
