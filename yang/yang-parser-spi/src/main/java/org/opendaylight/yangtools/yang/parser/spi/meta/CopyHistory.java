/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.MoreObjects;
import com.google.common.base.Verify;
import java.util.Arrays;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.model.api.CopyableNode;

@Beta
public final class CopyHistory implements Immutable, CopyableNode {
    private static final CopyType[] VALUES = CopyType.values();

    private static final CopyHistory[][] CACHE = new CopyHistory[VALUES.length][];

    static {
        /*
         * Cache size is dependent on number of items in CopyType, it costs N * 2^N objects.
         * For 4 types that boils down to 4 * 16 = 64 objects.
         * For 5 types that boils down to 5 * 32 = 160 objects.
         * For 6 types that boils down to 6 * 64 = 384 objects.
         *
         * If we ever hit 6 types, the caching strategy needs to be revisited.
         */
        Verify.verify(VALUES.length < 6);
    }

    private static final CopyHistory ORIGINAL = cacheObject(CopyType.ORIGINAL, CopyType.ORIGINAL.bit());
    private static final int IS_ADDED_BY_USES_BITS =
        CopyType.ADDED_BY_USES_AUGMENTATION.bit() | CopyType.ADDED_BY_USES.bit();
    private static final int IS_AUGMENTING_BITS =
        CopyType.ADDED_BY_USES_AUGMENTATION.bit() | CopyType.ADDED_BY_AUGMENTATION.bit();

    private final short operations;
    private final short lastOperation;

    private CopyHistory(final int operations, final CopyType lastOperation) {
        this.operations = (short) operations;
        this.lastOperation = (short) lastOperation.ordinal();
    }

    public static CopyHistory original() {
        return ORIGINAL;
    }

    public static CopyHistory of(final CopyType copyType, final CopyHistory copyHistory) {
        return ORIGINAL.append(copyType, copyHistory);
    }

    private static CopyHistory[] cacheArray(final CopyType lastOperation) {
        final int ordinal = lastOperation.ordinal();
        CopyHistory[] ret = CACHE[ordinal];
        if (ret == null) {
            synchronized (CACHE) {
                ret = CACHE[ordinal];
                if (ret == null) {
                    ret = new CopyHistory[1 << VALUES.length];
                    CACHE[ordinal] = ret;
                }
            }
        }

        return ret;
    }

    private static CopyHistory cacheObject(final CopyType lastOperation, final int operations) {
        final CopyHistory[] array = cacheArray(lastOperation);
        CopyHistory ret = array[operations];
        if (ret == null) {
            synchronized (array) {
                ret = array[operations];
                if (ret == null) {
                    ret = new CopyHistory(operations, lastOperation);
                    array[operations] = ret;
                }
            }
        }

        return ret;
    }

    public CopyType getLastOperation() {
        return VALUES[lastOperation];
    }

    @Override
    @Deprecated
    public boolean isAugmenting() {
        return (operations & IS_AUGMENTING_BITS) != 0;
    }

    @Override
    @Deprecated
    public boolean isAddedByUses() {
        return (operations & IS_ADDED_BY_USES_BITS) != 0;
    }

    @VisibleForTesting
    boolean contains(final CopyType type) {
        return (operations & type.bit()) != 0;
    }

    @VisibleForTesting
    CopyHistory append(final CopyType typeOfCopy, final CopyHistory toAppend) {
        final int newOperations = operations | toAppend.operations | typeOfCopy.bit();
        if (newOperations == operations && typeOfCopy.ordinal() == lastOperation) {
            return this;
        }

        return cacheObject(typeOfCopy, newOperations);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(operations | lastOperation << Short.SIZE);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof CopyHistory)) {
            return false;
        }
        final CopyHistory other = (CopyHistory) obj;
        return operations == other.operations && lastOperation == other.lastOperation;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("lastOperation", getLastOperation())
                .add("operations", Arrays.stream(VALUES).filter(value -> (value.bit() & operations) != 0)
                    .collect(Collectors.toList()))
                .toString();
    }
}
