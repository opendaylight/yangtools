/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import java.util.HashSet;
import java.util.Set;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Utility class which provides various helper methods for working with YANG
 * built-in types.
 *
 * @deprecated Use {@link BaseTypes} instead.
 */
@Deprecated
public final class YangTypesConverter {
    private static final Set<String> BASE_YANG_TYPES = new HashSet<String>();

    /**
     * It isn't desirable to create the instances of this class
     */
    private YangTypesConverter() {
    }

    static {
        BASE_YANG_TYPES.add("binary");
        BASE_YANG_TYPES.add("bits");
        BASE_YANG_TYPES.add("boolean");
        BASE_YANG_TYPES.add("decimal64");
        BASE_YANG_TYPES.add("empty");
        BASE_YANG_TYPES.add("enumeration");
        BASE_YANG_TYPES.add("identityref");
        BASE_YANG_TYPES.add("instance-identifier");
        BASE_YANG_TYPES.add("int8");
        BASE_YANG_TYPES.add("int16");
        BASE_YANG_TYPES.add("int32");
        BASE_YANG_TYPES.add("int64");
        BASE_YANG_TYPES.add("leafref");
        BASE_YANG_TYPES.add("string");
        BASE_YANG_TYPES.add("uint8");
        BASE_YANG_TYPES.add("uint16");
        BASE_YANG_TYPES.add("uint32");
        BASE_YANG_TYPES.add("uint64");
        BASE_YANG_TYPES.add("union");
    }

    @Deprecated
    public static boolean isBaseYangType(final String type) {
        return BaseTypes.isYangBuildInType(type);
    }

    /**
     *
     * Returns default instance of built-in type for supplied string.
     *
     * @param typeName
     * @return default instance of built-in type for supplied string or null, if
     *         default instance does not exist.
     *
     * @deprecated Use {@link BaseTypes#defaultBaseTypeFor(String)} instead.
     */
    @Deprecated
    public static TypeDefinition<?> javaTypeForBaseYangType(final String typeName) {
        return BaseTypes.defaultBaseTypeFor(typeName).orNull();
    }

}
