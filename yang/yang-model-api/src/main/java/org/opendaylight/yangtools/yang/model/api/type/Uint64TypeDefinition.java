/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import java.math.BigInteger;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Type definition derived from uint64 type.
 *
 * @author Robert Varga
 */
public interface Uint64TypeDefinition extends RangeRestrictedTypeDefinition<Uint64TypeDefinition, BigInteger> {

    static int hashCode(final @NonNull Uint64TypeDefinition type) {
        return TypeDefinitions.hashCode(type);
    }

    static boolean equals(final @NonNull Uint64TypeDefinition type, final @Nullable Object obj) {
        return TypeDefinitions.equals(Uint64TypeDefinition.class, type, obj);
    }

    static String toString(final @NonNull Uint64TypeDefinition type) {
        return TypeDefinitions.toString(type);
    }
}
