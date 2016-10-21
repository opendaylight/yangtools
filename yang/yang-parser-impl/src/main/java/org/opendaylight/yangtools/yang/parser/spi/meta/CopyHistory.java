/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.Verify;
import org.opendaylight.yangtools.concepts.Immutable;

@Beta
public final class CopyHistory implements Immutable {
    private static final CopyType[] VALUES = CopyType.values();

    private static final CopyHistory[][] CACHE;
    static {
        /*
         * Cache size is dependent on number of items in CopyType, it costs N * 2^N objects.
         * For 4 types that boils down to 4 * 16 = 54 objects.
         * For 5 types that boils down to 5 * 32 = 160 objects.
         * For 6 types that boils down to 6 * 64 = 384 objects.
         */
        Verify.verify(VALUES.length < 6);
        CACHE = new CopyHistory[VALUES.length][];
    }

    private static final CopyHistory ORIGINAL = cachedObject(CopyType.ORIGINAL, CopyType.ORIGINAL.bit());

    private final short operations;
    private final short lastOperation;

    private CopyHistory(final short operations, final CopyType lastOperation) {
        this.operations = operations;
        this.lastOperation = (short) lastOperation.ordinal();
    }

    public static CopyHistory original() {
        return ORIGINAL;
    }

    private static CopyHistory cachedObject(final CopyType lastOperation, final short operations) {
        final int ordinal = lastOperation.ordinal();
        CopyHistory[] array = CACHE[ordinal];
        if (array == null) {
            synchronized (CACHE) {
                array = CACHE[ordinal];
                if (array == null) {
                    array = new CopyHistory[1 << VALUES.length];
                    CACHE[ordinal] = array;
                }
            }
        }

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

    public boolean contains(final CopyType type) {
        return (operations & type.bit()) != 0;
    }

    public CopyType getLastOperation() {
        return VALUES[lastOperation];
    }

    public CopyHistory append(final CopyType typeOfCopy, final CopyHistory toAppend) {
        final short newOperations = (short)(operations | toAppend.operations | typeOfCopy.bit());
        if (newOperations == operations && typeOfCopy.ordinal() == lastOperation) {
            return this;
        }

        return cachedObject(typeOfCopy, newOperations);
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(operations | (lastOperation << Short.SIZE));
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
}
