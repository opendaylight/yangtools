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
import org.opendaylight.yangtools.yang.model.api.stmt.CaseEffectiveStatement;

@NonNullByDefault
record CaseObjectArchetypeImpl(
        JavaTypeName name,
        CaseEffectiveStatement statement,
        ChoiceInArchetype choice,
        List<Type> implementsTypes,
        List<TypeObjectArchetype<?>> typeObjects,
        List<MethodSignature> methodSignatures) implements CaseObjectArchetype {
    CaseObjectArchetypeImpl {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(implementsTypes);
        requireNonNull(typeObjects);
        requireNonNull(methodSignatures);
        requireNonNull(choice);
    }

    @Override
    public List<Type> getImplements() {
        return implementsTypes;
    }

    @Override
    public List<MethodSignature> getMethodDefinitions() {
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
        return TypeMethods.toStringHelper(CaseObjectArchetype.class, this).add("choice", choice.name()).toString();
    }
}
