/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.data;

import java.util.Arrays;
import java.util.Base64;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A {@code binary} value.
 */
@NonNullByDefault
public abstract non-sealed class Binary implements Comparable<Binary>, ScalarValue {

    public int size() {
        return bytes().length;
    }

    public byte[] toBytes() {
        return bytes().clone();
    }

    @Override
    public final int compareTo(final Binary other) {
        return Arrays.compare(bytes(), other.bytes());
    }

    @Override
    public final int hashCode() {
        return Arrays.hashCode(bytes());
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Binary other && Arrays.equals(bytes(), other.bytes());
    }

    @Override
    public final String toString() {
        return Base64.getEncoder().encodeToString(bytes());
    }

    protected abstract byte[] bytes();
}
