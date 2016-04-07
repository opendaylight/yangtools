/*
 * Copyright (c) 2014, 2016 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.Preconditions;
import java.util.function.Function;
import org.opendaylight.yangtools.concepts.Identifiable;

public final class Identifiables {
    private static <T> T extractIdentifier(Identifiable<T> input) {
        Preconditions.checkNotNull(input);
        return input.getIdentifier();
    }

    private Identifiables() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Return the {@link Function} to extract the identifier from a particular
     * object implementing the {@link Identifiable} contract.
     *
     * @return Identificator associated with the object
     */
    public static <V> Function<Identifiable<V>, V> identifierExtractor() {
        return Identifiables::extractIdentifier;
    }
}

