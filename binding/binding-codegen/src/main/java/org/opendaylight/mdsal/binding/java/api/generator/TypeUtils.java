/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.mdsal.binding.java.api.generator;

import static com.google.common.base.Preconditions.checkArgument;

import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.mdsal.binding.model.api.ConcreteType;
import org.opendaylight.mdsal.binding.model.api.GeneratedProperty;
import org.opendaylight.mdsal.binding.model.api.GeneratedTransferObject;
import org.opendaylight.mdsal.binding.model.api.Type;
import org.opendaylight.mdsal.binding.model.ri.TypeConstants;

/**
 * Random utility methods for dealing with {@link Type} objects.
 */
final class TypeUtils {
    private TypeUtils() {

    }

    /**
     * Given a {@link Type} object lookup the base Java type which sits at the top
     * of its type hierarchy.
     *
     * @param type Input Type object
     * @return Resolved {@link ConcreteType} instance.
     */
    static ConcreteType getBaseYangType(final @NonNull Type type) {
        // Already the correct type
        if (type instanceof ConcreteType concrete) {
            return concrete;
        }

        checkArgument(type instanceof GeneratedTransferObject, "Unsupported type %s", type);

        // Need to walk up the GTO chain to the root
        GeneratedTransferObject rootGto = (GeneratedTransferObject) type;
        while (rootGto.getSuperType() != null) {
            rootGto = rootGto.getSuperType();
        }

        // Look for the 'value' property and return its type
        for (GeneratedProperty s : rootGto.getProperties()) {
            if (TypeConstants.VALUE_PROP.equals(s.getName())) {
                return (ConcreteType) s.getReturnType();
            }
        }

        // Should never happen
        throw new IllegalArgumentException(String.format("Type %s root %s properties %s do not include \"%s\"",
            type, rootGto, rootGto.getProperties(), TypeConstants.VALUE_PROP));
    }

    static Type encapsulatedValueType(final GeneratedTransferObject gto) {
        return gto.findProperty(TypeConstants.VALUE_PROP).orElseThrow().getReturnType();
    }
}
