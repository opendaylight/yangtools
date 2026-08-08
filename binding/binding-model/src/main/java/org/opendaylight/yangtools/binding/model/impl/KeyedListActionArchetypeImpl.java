/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.KeyedListActionArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcInputArchetype;
import org.opendaylight.yangtools.binding.model.api.RpcOutputArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.ActionEffectiveStatement;

@NonNullByDefault
public record KeyedListActionArchetypeImpl(
        TypeName name,
        ActionEffectiveStatement statement,
        RpcInputArchetype input,
        RpcOutputArchetype output,
        TypeName parentName) implements KeyedListActionArchetype {
    public KeyedListActionArchetypeImpl {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(input);
        requireNonNull(output);
        requireNonNull(parentName);
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
        return TypeMethods.toString(KeyedListActionArchetype.class, this);
    }
}
