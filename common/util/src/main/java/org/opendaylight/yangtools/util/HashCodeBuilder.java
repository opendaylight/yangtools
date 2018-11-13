/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.concepts.Builder;

/**
 * Utility class for incrementally building object hashCode by hashing together component objects, one by one.
 *
 * @param <T> Component object type
 */
public final class HashCodeBuilder<T> implements Builder<Integer> {
    /**
     * The value 31 was chosen because it is an odd prime. If it were even and the multiplication overflowed,
     * information would be lost, as multiplication by 2 is equivalent to shifting. The advantage of using a prime is
     * less clear, but it is traditional. A nice property of 31 is that the multiplication can be replaced by a shift
     * and a subtraction for better performance: 31 * i == (i << 5) - i. Modern VMs do this sort of optimization
     * automatically.
     *
     * <p>(from Joshua Bloch's Effective Java, Chapter 3, Item 9: Always override hashcode when you override equals,
     * page 48)
     */
    private static final int PRIME = 31;
    private int currentHash;

    /**
     * Create a new instance, with internal hash initialized to 1, equivalent of <code>HashCodeBuilder(1)</code>.
     */
    public HashCodeBuilder() {
        this(1);
    }

    /**
     * Create a new instance, with internal hash set to specified seed.
     *
     * @param seedHash Seed hash value
     */
    public HashCodeBuilder(final int seedHash) {
        this.currentHash = seedHash;
    }

    /**
     * Determine the next hash code combining a base hash code and the hash code of an object.
     *
     * @param hashCode base hash code
     * @param obj Object to be added
     * @return Combined hash code
     */
    public static int nextHashCode(final int hashCode, final Object obj) {
        return PRIME * hashCode + obj.hashCode();
    }

    /**
     * Update the internal hash code with the hash code of a component object.
     *
     * @param obj Component object
     */
    public void addArgument(final T obj) {
        currentHash = nextHashCode(currentHash, obj);
    }

    @Override
    public @NonNull Integer build() {
        return currentHash;
    }
}
