/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyArgument;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.LeafEffectiveStatement;

record KeyArchetypeImpl(
        @NonNull JavaTypeName name,
        @NonNull KeyEffectiveStatement statement,
        @NonNull EntryObjectArchetype entryObject) implements KeyArchetype {
    KeyArchetypeImpl {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(entryObject);
    }

    @Override
    public JavaTypeName entryObjectName() {
        return entryObject.name();
    }

    @Override
    public Map<String, MethodSignature> methods() {
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

    static HashMap<String, MethodSignature> collectMethods(final KeyArgument keyArgument,
            final EntryObjectArchetype entryObject) {
        final var keyNames = keyArgument.stream().map(QName::getLocalName).collect(Collectors.toSet());
        final var keyToMethod = HashMap.<String, MethodSignature>newHashMap(keyNames.size());
        collectMethods(keyNames, keyToMethod, entryObject);
        return keyToMethod;
    }

    private static void collectMethods(final Set<@NonNull String> keyNames,
            final HashMap<String, MethodSignature> keyToMethod, final DataContainerArchetype archetype) {
        for (var method : archetype.getMethodDefinitions()) {
            // FIXME: remove the && check when we emit only getters
            if (method.statement() instanceof LeafEffectiveStatement leaf && Naming.isGetterMethodName(method.name())) {
                final var localName = leaf.argument().getLocalName();
                if (keyNames.contains(localName)) {
                    keyToMethod.putIfAbsent(localName, method);
                }
            }
        }
        for (var partial : archetype.partials()) {
            collectMethods(keyNames, keyToMethod, partial);
        }
    }
}
