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
import org.opendaylight.yangtools.yang.common.YangConstants;

/**
 * Type definition derived from int8 type.
 *
 * @author Robert Varga
 */
public interface Int8TypeDefinition extends RangeRestrictedTypeDefinition<Int8TypeDefinition, Byte> {
    /**
     * Well-known QName of the {@code int8} built-in type.
     */
    QName QNAME = QName.create(YangConstants.RFC6020_YANG_MODULE, "int8").intern();

    static int hashCode(final @NonNull Int8TypeDefinition type) {
        return TypeDefinitions.hashCode(type);
    }

    static boolean equals(final @NonNull Int8TypeDefinition type, final @Nullable Object obj) {
        return TypeDefinitions.equals(Int8TypeDefinition.class, type, obj);
    }

    static String toString(final @NonNull Int8TypeDefinition type) {
        return TypeDefinitions.toString(type);
    }
}
