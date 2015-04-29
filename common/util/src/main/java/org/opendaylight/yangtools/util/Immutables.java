/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Set;
import org.opendaylight.yangtools.concepts.Immutable;
import org.opendaylight.yangtools.concepts.Mutable;

public final class Immutables {

    private Immutables() {
        throw new UnsupportedOperationException("Helper class");
    }

    public static final Set<Class<?>> KNOWN_IMMUTABLES = ImmutableSet.<Class<?>>of(
            Integer.class, Short.class, BigDecimal.class, BigInteger.class, Byte.class, Character.class, Double.class,
            Float.class, String.class, Boolean.class, Void.class);

    /**
     * Determines if object is known to be immutable
     *
     * Note: This method may return false to immutable objects which
     * immutability is not known, was defined not using concepts term.
     *
     * @param o
     *            Reference to check
     * @return true if object is known to be immutable false otherwise.
     */
    public static boolean isImmutable(final Object o) {
        Preconditions.checkArgument(o != null,"Object should not be null");
        if (o instanceof Mutable) {
            return false;
        } else if (o instanceof Immutable) {
            return true;
        } else if (o instanceof String) {
            return true;
        } else if (KNOWN_IMMUTABLES.contains(o.getClass())) {
            return true;
        }
        return false;
    }
}
