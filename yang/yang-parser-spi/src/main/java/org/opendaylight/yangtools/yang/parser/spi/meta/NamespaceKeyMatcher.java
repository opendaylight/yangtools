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
 * A loose key matcher instance. Implementations of this interface are responsible for performing the actual
 * key matching associated with loose keys. For each candidate key, an implementation should return
 * a {@link Decision}. Implementations can be stateful and select the best candidate key, potentially terminating
 * searches early.
 *
 * @param <K> Key type
 *
 * @author Robert Varga
 */
@Beta
@FunctionalInterface
public interface NamespaceKeyMatcher<K> {
    /**
     * Key matching decision made by a {@link KeyMatcher}.
     */
    enum Decision {
        /**
         * Supplied key was accepted and is better than the previous match.
         */
        ACCEPT,
        /**
         * Supplied key was rejected, any previous match is still the best match so far.
         */
        REJECT,
        /**
         * Supplied key was accepted and it should be the result of this match operation, e.g. no further matching
         * should be done.
         */
        RESULT,
    }

    /**
     * Match a key against this matcher and provide a decision to guide the matching logic.
     *
     * @param key Key to be matched
     * @return Matcher decision
     */
    @Nonnull Decision match(@Nonnull K key);
}
