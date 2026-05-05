/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import java.util.Optional;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;

/**
 * Generated Transfer Object extends {@link GeneratedType} and is designed to represent Java Class. The Generated
 * Transfer Object contains declarations of member fields stored in List of Properties. The Generated Transfer Object
 * can be extended by exactly ONE Generated Transfer Object as Java does not allow multiple inheritance. For retrieval
 * of implementing Generated Types use {@link #getImplements()} method.
 */
// FIXME: sealed
// FIXME: rename to TOArchetype and extends TypeObjectArchetype
// FIXME: update documentation
public interface GeneratedTransferObject extends GeneratedType {
    /**
     * {@return the value of the {@code serialVersionUID} of this {@link TypeObject} class};
     */
    default long serialVersionUID() {
        return SerialVersionHelper.computeSerialVersion(this);
    }

    /**
     * Returns the Generated Transfer Object from which this GTO is derived, or null if this GTO is not derived
     * from a GTO -- e.g. it is either an union or it is derived from a concrete type.
     *
     * @return Generated Transfer Object or <code>null</code> if this GTO is not derived from another GTO.
     */
    @Nullable GeneratedTransferObject getSuperType();

    boolean isTypedef();

    /**
     * Returns Base type of Java representation of YANG typedef if set, otherwise it returns null.
     *
     * @return Base type of Java representation of YANG typedef if set, otherwise it returns null
     */
    TypeDefinition<?> getBaseType();

    default Restrictions getRestrictions() {
        throw uoe();
    }

    @Override
    default String getDescription() {
        throw uoe();
    }

    @Override
    default String getReference() {
        throw uoe();
    }

    @Override
    default String getModuleName() {
        throw uoe();
    }

    private static UnsupportedOperationException uoe() {
        return new UnsupportedOperationException("Not available at runtime");
    }

    default Optional<? extends GeneratedProperty> findProperty(final String name) {
        final Optional<GeneratedProperty> optProp = getProperties().stream()
                .filter(prop -> prop.getName().equals(name)).findFirst();
        if (optProp.isPresent()) {
            return optProp;
        }

        final GeneratedTransferObject parent = getSuperType();
        return parent != null ? parent.findProperty(name) : Optional.empty();
    }
}
