/*
 * Copyright (c) 2020 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.tree.impl;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;
import java.util.Base64;
import org.opendaylight.yangtools.concepts.Immutable;

final class BinaryValue implements Immutable {
    private final byte[] value;

    private BinaryValue(final byte[] value) {
        this.value = requireNonNull(value);
    }

    static Object unwrap(final Object value) {
        return value instanceof BinaryValue binary ? binary.value : value;
    }

    static Object wrap(final Object value) {
        return value instanceof byte[] bytes ? new BinaryValue(bytes) : value;
    }

    static Object wrapToString(final Object value) {
        return value instanceof byte[] bytes ? toString(bytes) : value;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(value);
    }

    @Override
    public boolean equals(final Object obj) {
        return obj == this || obj instanceof BinaryValue other && Arrays.equals(value, other.value);
    }

    @Override
    public String toString() {
        return toString(value);
    }

    private static String toString(final byte[] value) {
        return Base64.getEncoder().encodeToString(value);
    }
}
