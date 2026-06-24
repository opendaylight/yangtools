/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.ConcreteType;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.ScalarTypeObjectArchetype;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.binding.model.ri.TypeConstants;

/**
 * Random utility methods for dealing with {@link Type} objects.
 */
final class TypeUtils {
    private TypeUtils() {
        // Hidden on purpose
    }

    /**
     * Given a {@link Type} object lookup the base Java type which sits at the top
     * of its type hierarchy.
     *
     * @param type Input Type object
     * @return Resolved {@link ConcreteType} instance.
     */
    static ConcreteType getBaseYangType(final @NonNull Type type) {
        return switch (type) {
            case ConcreteType concrete -> concrete;
            case ScalarTypeObjectArchetype scalar -> scalar.valueType();
            default -> throw new IllegalArgumentException("Unsupported type " + type);
        };
    }

    @NonNullByDefault
    static Type encapsulatedValueType(final GeneratedTransferObject<?> gto) {
        return gto.findProperty(TypeConstants.VALUE_PROP).orElseThrow().getReturnType();
    }
}
