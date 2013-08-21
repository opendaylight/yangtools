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
    private static final Set<String> baseYangTypes = new HashSet<String>();

    static {
        baseYangTypes.add("binary");
        baseYangTypes.add("bits");
        baseYangTypes.add("boolean");
        baseYangTypes.add("decimal64");
        baseYangTypes.add("empty");
        baseYangTypes.add("enumeration");
        baseYangTypes.add("identityref");
        baseYangTypes.add("instance-identifier");
        baseYangTypes.add("int8");
        baseYangTypes.add("int16");
        baseYangTypes.add("int32");
        baseYangTypes.add("int64");
        baseYangTypes.add("leafref");
        baseYangTypes.add("string");
        baseYangTypes.add("uint8");
        baseYangTypes.add("uint16");
        baseYangTypes.add("uint32");
        baseYangTypes.add("uint64");
        baseYangTypes.add("union");
    }

    public static boolean isBaseYangType(String type) {
        return baseYangTypes.contains(type);
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
        } else if("binary".equals(typeName)) {
            type = BinaryType.getInstance();
        } else if("boolean".equals(typeName)) {
            type = BooleanType.getInstance();
        } else if("empty".equals(typeName)) {
            type = EmptyType.getInstance();
        } else if("instance-identifier".equals(typeName)) {
            // FIXME
            type = new InstanceIdentifier(null, true);
        }

        return type;
    }

}
