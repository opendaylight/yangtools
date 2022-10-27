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
    protected int @NonNull [] setCurrentBits(final Set<String> strings, final List<String> validBits) {
        // FIXME: Needs implementation
        return new int[0];
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
        // FIXME: This is not correct
        final int[] allBitsSet = new int[sizeOfBits];
        for (int i = 0; i < sizeOfBits; i++) {
            setBit(allBitsSet, true, i);
        }
        return allBitsSet;
    }

    @Override
    protected boolean isBitSet(final int position, final int[] bits) {
        // FIXME: Needs implementation
        return false;
    }

    @Override
    protected int getLengthOfBits(final int[] bits) {
        // FIXME: Needs implementation
        return 0;
    }

}
