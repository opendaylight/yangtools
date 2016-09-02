/*
 * Copyright (c) 2014 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.util;

import com.google.common.base.Function;
import org.opendaylight.yangtools.concepts.Identifiable;

@Deprecated
public final class Identifiables {
    private Identifiables() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Return the {@link Function} to extract the identifier from a particular
     * object implementing the {@link Identifiable} contract.
     *
     * @return Identifier associated with the object
     *
     * @deprecated Use Identifiable::getIdentifier instead
     */
    @Deprecated
    public static <V> Function<Identifiable<V>, V> identifierExtractor() {
        return Identifiable::getIdentifier;
    }
}

