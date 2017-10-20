/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.spi.meta;

import com.google.common.annotations.Beta;
import javax.annotation.Nonnull;

/**
 * Namespace key matching criterion.
 *
 * @param <K> Key type
 *
 * @author Robert Varga
 */
@Beta
public abstract class NamespaceKeyCriterion<K> {
    /**
     * Match a key against this criterion.
     *
     * @param key Key to be matched
     * @return True if the key matches this criterion, false otherwise.
     */
    public abstract boolean match(@Nonnull K key);

    /**
     * Select the better match from two candidate keys.
     *
     * @param first First key
     * @param second Second key
     * @return Selected key, must be either first or second key, by identity.
     */
    public abstract K select(@Nonnull K first, @Nonnull K second);

    @Override
    public abstract String toString();

    @Override
    public final int hashCode() {
        return super.hashCode();
    }

    @Override
    public final boolean equals(final Object obj) {
        return super.equals(obj);
    }
}
