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

final class BitsLongCodec extends AbstractBitsNumberCodec<Long> implements BitsNumberCodec<String, Long> {

    BitsLongCodec(@Nullable final BitsTypeDefinition typeDef, final Class<Long> outputClass) {
        super(typeDef, outputClass);
    }

    @Override
    protected @NonNull Long normalizeBits(final Set<String> strings, final List<String> sortedBits) {
        long currentBits = 0;
        for (final String bit : sortedBits) {
            currentBits = setBit(currentBits, strings.contains(bit), sortedBits.indexOf(bit));
        }
        return currentBits;
    }

    @Override
    protected @NonNull Long setBit(final Long bits, final boolean value, final int position) {
        validatePosition(position);
        return value ? bits | 1L << position : bits & ~(1L << position);
    }

    @Override
    protected @NonNull Long setAllBits(final int sizeOfBits) {
        long allBitsSet = 0;
        for (int i = 0; i < sizeOfBits; i++) {
            allBitsSet = setBit(allBitsSet, true, i);
        }
        return allBitsSet;
    }

    @Override
    protected boolean isBitSet(int position, Long bits) {
        return (bits & (1L << position)) != 0;
    }

    @Override
    protected int getLengthOfBits(final Long bits) {
        return Long.bitCount(bits);
    }

}
