/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.sal.java.api.generator;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.Map;
import org.opendaylight.yangtools.sal.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.sal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.sal.binding.model.api.Type;
import org.opendaylight.yangtools.sal.binding.yang.types.BaseYangTypes;

/**
 * Possible classes of complexity, ordered in the order of ascending
 * computational requirements.
 */
enum EqualsComplexity {
    // Simple comparison
    BOOLEAN,
    BYTE,
    SHORT,
    INT,
    ENUM,
    IDENTITYREF,
    LONG,

    // Multiple simple comparison
    BIGINTEGER,
    STRING,
    BINARY,
    BITS,
    UNION,

    // Potential allocation
    BIGDECIMAL,

    // Potential recursion
    INSTANCEIDENTIFIER,
    CONTAINER;

    private static final Map<String, EqualsComplexity> JAVATYPE_TO_COMPLEXITY;

    static {
        final Builder<String, EqualsComplexity> b = ImmutableMap.<String, EqualsComplexity>builder();
        b.put(BaseYangTypes.BOOLEAN_TYPE.getFullyQualifiedName(), BOOLEAN);
        b.put(BaseYangTypes.INT8_TYPE.getFullyQualifiedName(), BYTE);
        b.put(BaseYangTypes.INT16_TYPE.getFullyQualifiedName(), SHORT);
        b.put(BaseYangTypes.INT32_TYPE.getFullyQualifiedName(), INT);
        b.put(BaseYangTypes.ENUM_TYPE.getFullyQualifiedName(), ENUM);
        b.put(BaseYangTypes.INT64_TYPE.getFullyQualifiedName(), LONG);
        b.put(BaseYangTypes.UINT64_TYPE.getFullyQualifiedName(), BIGINTEGER);
        b.put(BaseYangTypes.STRING_TYPE.getFullyQualifiedName(), STRING);
        b.put(BaseYangTypes.BINARY_TYPE.getFullyQualifiedName(), BINARY);
        b.put(BaseYangTypes.DECIMAL64_TYPE.getFullyQualifiedName(), BIGDECIMAL);
        b.put(BaseYangTypes.INSTANCE_IDENTIFIER.getFullyQualifiedName(), INSTANCEIDENTIFIER);

        JAVATYPE_TO_COMPLEXITY = b.build();
    }

    private static EqualsComplexity forConcreteType(final ConcreteType type) {
        return JAVATYPE_TO_COMPLEXITY.get(type.getFullyQualifiedName());
    }

    static EqualsComplexity forType(final Type type) {
        if (type instanceof GeneratedTransferObject) {
            final GeneratedTransferObject gto = (GeneratedTransferObject) type;
            if (gto.isUnionType()) {
                return UNION;
            }

            // FIXME: what else?
        }

        final EqualsComplexity lookup = forConcreteType(TypeUtils.getBaseYangType(type));
        if (lookup != null) {
            return lookup;
        }

        // FIXME:
        // IDENTITYREF
        // BITS
        // CONTAINER

        return CONTAINER;
    }
}