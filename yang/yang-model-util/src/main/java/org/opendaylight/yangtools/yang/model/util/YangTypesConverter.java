/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util;

import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Utility class which provides various helper methods for working with YANG
 * built-in types.
 *
 * @deprecated Use {@link BaseTypes} instead.
 */
@Deprecated
public final class YangTypesConverter {
    /**
     * It isn't desirable to create the instances of this class
     */
    private YangTypesConverter() {
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
