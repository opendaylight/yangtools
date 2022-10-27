/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import java.util.List;
import java.util.Set;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.data.api.codec.BitsNumberCodec;
import org.opendaylight.yangtools.yang.model.api.type.BitsTypeDefinition;

final class BitsIntArrayCodec extends AbstractBitsNumberCodec<int[]> implements BitsNumberCodec<String, int[]> {
    private static final int ALL_SET_BITS = 0xFFFFFFFF;

    private static final int WORD_SIZE = 32;

    BitsIntArrayCodec(@Nullable final BitsTypeDefinition typeDef, final Class<int[]> outputClass) {
        super(typeDef, outputClass);
    }

    @Override
    protected int @NonNull [] normalizeBits(final Set<String> strings, final List<String> sortedBits) {
        final int length = calculateLength(sortedBits.size());
        final int[] currentBits = new int[length];

        for (final String bit : sortedBits) {
            setBit(currentBits, strings.contains(bit), sortedBits.indexOf(bit));
        }
        return currentBits;
    }

    @Override
    protected int @NonNull [] setBit(final int[] bits, final boolean value, final int position) {
        validatePosition(position);
        final int word = bits[position / WORD_SIZE];
        final int shifted = 1 << (position % WORD_SIZE);
        bits[position / WORD_SIZE] = value ? word | shifted : word & (ALL_SET_BITS - shifted);
        return bits;
    }

    @Override
    protected int @NonNull [] setAllBits(final int sizeOfBits) {
        final int length = calculateLength(sizeOfBits);
        final int[] allBitsSet = new int[length];

        for (int i = 0; i < sizeOfBits; i++) {
            setBit(allBitsSet, true, i);
        }
        return allBitsSet;
    }

    @Override
    protected boolean isBitSet(final int position, final int[] bits) {
        final int word = bits[position / WORD_SIZE];
        return (word & 1 << (position % WORD_SIZE)) != 0;
    }

    @Override
    protected int getLengthOfBits(final int[] bits) {
        int length = 0;
        for (final int word : bits) {
            final int wordLength = Integer.bitCount(word);
            length += wordLength;
        }
        return length;
    }

    private static int calculateLength(final int size) {
        return size / WORD_SIZE + (size % WORD_SIZE == 0 ? 0 : 1);
    }

}
