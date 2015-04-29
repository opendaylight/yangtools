/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;
import org.opendaylight.yangtools.concepts.Identifiable;

public final class Identifiables {
    private static final Function<Identifiable<Object>, Object> EXTRACT_IDENTIFIER = new Function<Identifiable<Object>, Object>() {
        @Override
        public Object apply(@Nonnull final Identifiable<Object> input) {
            Preconditions.checkNotNull(input);
            return input.getIdentifier();
        }
    };

    private Identifiables() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Return the {@link Function} to extract the identifier from a particular
     * object implementing the {@link Identifiable} contract.
     *
     * @return Identificator associated with the object
     */
    /*
     * Suppressing warnings here allows us to fool the compiler enough
     * such that we can reuse a single function for all applicable types
     * and present it in a type-safe manner to our users.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public static <V> Function<Identifiable<V>, V> identifierExtractor() {
        return (Function) EXTRACT_IDENTIFIER;
    }
}

