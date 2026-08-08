/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashMap;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.DataContainerArchetype;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.GetterMethod;
import org.opendaylight.yangtools.binding.model.KeyArchetype;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;

public record KeyArchetypeImpl(
        @NonNull TypeName name,
        @NonNull KeyEffectiveStatement statement,
        @NonNull EntryObjectArchetype entryObject) implements KeyArchetype {
    public KeyArchetypeImpl {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(entryObject);
    }

    @Override
    public Map<String, GetterMethod> methods() {
        return collectMethods(statement.argument(), entryObject);
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    // TODO: iteration order needs to match argument declaration order to retain compatibility, but perhaps we can
    //       relax that
    public static LinkedHashMap<String, @Nullable GetterMethod> collectMethods(final KeyArgument keyArgument,
            final EntryObjectArchetype entryObject) {
        final var keyToMethod = LinkedHashMap.<String, @Nullable GetterMethod>newLinkedHashMap(keyArgument.size());
        for (var qname : keyArgument) {
            keyToMethod.put(qname.getLocalName(), null);
        }
        collectMethods(keyToMethod, entryObject);
        return keyToMethod;
    }

    private static void collectMethods(final LinkedHashMap<String, @Nullable GetterMethod> keyToMethod,
            final DataContainerArchetype archetype) {
        for (var method : archetype.getters()) {
            if (method.statement() instanceof LeafEffectiveStatement leaf) {
                keyToMethod.replace(leaf.argument().getLocalName(), null, method);
            }
        }
        for (var partial : archetype.partials()) {
            collectMethods(keyToMethod, partial);
        }
    }
}
