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

    public static boolean isBaseYangType(String type) {
        return BASE_YANG_TYPES.contains(type);
    }

    public static TypeDefinition<?> javaTypeForBaseYangType(String typeName) {
        TypeDefinition<?> type = null;

        if (typeName.startsWith("int")) {
            if ("int8".equals(typeName)) {
                type = Int8.getInstance();
            } else if ("int16".equals(typeName)) {
                type = Int16.getInstance();
            } else if ("int32".equals(typeName)) {
                type = Int32.getInstance();
            } else if ("int64".equals(typeName)) {
                type = Int64.getInstance();
            }
        } else if (typeName.startsWith("uint")) {
            if ("uint8".equals(typeName)) {
                type = Uint8.getInstance();
            } else if ("uint16".equals(typeName)) {
                type = Uint16.getInstance();
            } else if ("uint32".equals(typeName)) {
                type = Uint32.getInstance();
            } else if ("uint64".equals(typeName)) {
                type = Uint64.getInstance();
            }
        } else if ("string".equals(typeName)) {
            type = StringType.getIntance();
        } else if ("binary".equals(typeName)) {
            type = BinaryType.getInstance();
        } else if ("boolean".equals(typeName)) {
            type = BooleanType.getInstance();
        } else if ("empty".equals(typeName)) {
            type = EmptyType.getInstance();
        } else if ("instance-identifier".equals(typeName)) {
            // FIXME
            type = new InstanceIdentifier(null, true);
        }

        return type;
    }

}
