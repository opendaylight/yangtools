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
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Type definition derived from uint16 type.
 *
 * @author Robert Varga
 */
public interface Uint16TypeDefinition extends RangeRestrictedTypeDefinition<Uint16TypeDefinition, Uint16> {
    /**
     * Well-known QName of the {@code uint16} built-in type.
     */
    QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, "uint16").intern();

    static int hashCode(final @NonNull Uint16TypeDefinition type) {
        return TypeDefinitions.hashCode(type);
    }

    static boolean equals(final @NonNull Uint16TypeDefinition type, final @Nullable Object obj) {
        return TypeDefinitions.equals(Uint16TypeDefinition.class, type, obj);
    }

    static String toString(final @NonNull Uint16TypeDefinition type) {
        return TypeDefinitions.toString(type);
    }
}
