/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.binding;

import org.opendaylight.yangtools.concepts.Builder;

final class HashCodeBuilder<T> implements Builder<Integer> {
    private int currentHash;

    public HashCodeBuilder() {
        this(1);
    }

    public HashCodeBuilder(final int seedHash) {
        this.currentHash = seedHash;
    }

    public static int nextHashCode(final int hashCode, final Object arg) {
        return 31 * hashCode + arg.hashCode();
    }

    void addArgument(final T arg) {
        currentHash = nextHashCode(currentHash, arg);
    }

    @Override
    public Integer toInstance() {
        return currentHash;
    }
}
