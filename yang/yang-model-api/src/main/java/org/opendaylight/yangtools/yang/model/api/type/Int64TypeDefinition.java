/*
 * Copyright (c) 2017 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.api.type;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Type definition derived from int64 type.
 *
 * @author Robert Varga
 */
public interface Int64TypeDefinition extends RangeRestrictedTypeDefinition<Int64TypeDefinition, Long> {

    static int hashCode(final @NonNull Int64TypeDefinition type) {
        return TypeDefinitions.hashCode(type);
    }

    static boolean equals(final @NonNull Int64TypeDefinition type, final @Nullable Object obj) {
        return TypeDefinitions.equals(Int64TypeDefinition.class, type, obj);
    }

    static String toString(final @NonNull Int64TypeDefinition type) {
        return TypeDefinitions.toString(type);
    }
}
