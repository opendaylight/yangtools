/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.CaseObjectArchetype;
import org.opendaylight.yangtools.binding.model.ChoiceInArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.ChoiceEffectiveStatement;

@NonNullByDefault
public record ChoiceInArchetype1(
        TypeName name,
        ChoiceEffectiveStatement statement,
        TypeName parentName,
        CaseObjectArchetype onlyCase) implements ChoiceInArchetype {
    public ChoiceInArchetype1 {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(parentName);
        requireNonNull(onlyCase);
    }

    @Override
    public List<CaseObjectArchetype> cases() {
        return List.of(onlyCase);
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
        return TypeMethods.toString(this);
    }
}
