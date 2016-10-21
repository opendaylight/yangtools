/*
 * Copyright (c) 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import com.google.common.base.Preconditions;
import com.google.common.base.Verify;
import com.google.common.collect.Interner;
import com.google.common.collect.Interners;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext.TypeOfCopy;

@Beta
public final class CopyHistory implements Immutable {
    private static final int ORIGINAL_BIT;
    static {
        Verify.verify(TypeOfCopy.values().length < 32);
        ORIGINAL_BIT = bit(TypeOfCopy.ORIGINAL);
    }

    private static final CopyHistory ORIGINAL = new CopyHistory(ORIGINAL_BIT, TypeOfCopy.ORIGINAL);
    private static final Interner<CopyHistory> INTERNER = Interners.newWeakInterner();

    private final TypeOfCopy lastOperation;
    private final int operations;

    private CopyHistory(final int operations, final TypeOfCopy lastOperation) {
        this.operations = operations;
        this.lastOperation = Preconditions.checkNotNull(lastOperation);
    }

    public static CopyHistory original() {
        return ORIGINAL;
    }

    public boolean contains(final TypeOfCopy type) {
        return (operations & bit(type)) != 0;
    }

    public TypeOfCopy getLastOperation() {
        return lastOperation;
    }

    public CopyHistory append(final TypeOfCopy typeOfCopy, final CopyHistory toAppend) {
        final int newOperations = operations | toAppend.operations | bit(typeOfCopy);
        if (newOperations == ORIGINAL_BIT) {
            return ORIGINAL;
        }
        if (newOperations == operations && typeOfCopy == lastOperation) {
            return this;
        }

        return INTERNER.intern(new CopyHistory(newOperations, typeOfCopy));
    }

    private static int bit(final TypeOfCopy type) {
        return 1 << type.ordinal();
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(operations) * 31 + lastOperation.hashCode();
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
