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

final class BitsIntegerCodec extends AbstractBitsNumberCodec<Integer> implements BitsNumberCodec<String, Integer> {

    BitsIntegerCodec(@Nullable final BitsTypeDefinition typeDef, final Class<Integer> outputClass) {
        super(typeDef, outputClass);
    }

    @Override
    protected @NonNull Integer setCurrentBits(final Set<String> strings, final List<String> sortedBits) {
        int currentBits = 0;
        for (final String bit : sortedBits) {
            currentBits = setBit(currentBits, strings.contains(bit), sortedBits.indexOf(bit));
        }
        return currentBits;
    }

    @Override
    protected @NonNull Integer setBit(final Integer bits, final boolean value, final int position) {
        validatePosition(position);
        return value ? bits | 1 << position : bits & ~(1 << position);
    }

    @Override
    protected @NonNull Integer setAllBits(final int sizeOfBits) {
        int allBitsSet = 0;
        for (int i = 0; i < sizeOfBits; i++) {
            allBitsSet = setBit(allBitsSet, true, i);
        }
        return allBitsSet;
    }

    @Override
    protected boolean isBitSet(final int position, final Integer bits) {
        return (bits & (1 << position)) != 0;
    }

    @Override
    protected int getLengthOfBits(final Integer bits) {
        if (bits == 0) {
            return 0;
        }
        final int positionOfLastBit = Integer.numberOfTrailingZeros(Integer.highestOneBit(bits));
        return positionOfLastBit + 1;
    }

}
