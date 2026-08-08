/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ContainerEffectiveStatement;

@NonNullByDefault
record ContainerObjectArchetypeImpl(
        TypeName name,
        ContainerEffectiveStatement statement,
        TypeName parentName,
        List<Partial> partials,
        List<TypeObjectArchetype<?>> typeObjects,
        List<GetterMethod> methodSignatures) implements ContainerObjectArchetype {
    ContainerObjectArchetypeImpl {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(partials);
        requireNonNull(typeObjects);
        requireNonNull(methodSignatures);
    }

    @Override
    public List<GetterMethod> getters() {
        return methodSignatures;
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    @Override
    public String toString() {
        return TypeMethods.toString(ContainerObjectArchetype.class, this);
    }
}
