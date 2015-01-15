/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import org.opendaylight.yangtools.concepts.Builder;

/**
 * Utility class for incrementally building object hashCode by hashing together
 * component objects, one by one.
 *
 * @param <T> Component objec type
 */
public final class HashCodeBuilder<T> implements Builder<Integer> {
    private int currentHash;

    /**
     * Create a new instance, with internal hash initialized to 1,
     * equivalent of <code>HashCodeBuilder(1)</code>.
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
     * Determine the next hash code combining a base hash code and the
     * hash code of an object.
     *
     * @param hashCode base hash code
     * @param obj Object to be added
     * @return Combined hash code
     */
    public static int nextHashCode(final int hashCode, final Object obj) {
        return 31 * hashCode + obj.hashCode();
    }

    /**
     * Update the internal hash code with the hash code of a component
     * object.
     *
     * @param obj Component object
     */
    public void addArgument(final T obj) {
        currentHash = nextHashCode(currentHash, obj);
    }

    @Override
    public Integer build() {
        return currentHash;
    }

    @Deprecated
    public Integer toInstance() {
        return currentHash;
    }
}
