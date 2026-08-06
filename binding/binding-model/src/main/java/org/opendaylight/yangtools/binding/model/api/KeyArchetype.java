/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.annotations.Beta;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;

/**
 * An archetype for a {@link Key} attached to an {@link EntryObject}.
 *
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public sealed interface KeyArchetype extends Archetype permits KeyArchetypeImpl {
    @Override
    KeyEffectiveStatement statement();

    JavaTypeName entryObjectName();

    Map<String, GetterMethod> methods();

    static KeyArchetype of(final JavaTypeName name, final KeyEffectiveStatement statement,
            final EntryObjectArchetype entryObject) {
        final var eoName = entryObject.name();
        if (!name.packageName().equals(eoName.packageName()) || !name.localName().startsWith(eoName.localName())) {
            throw new IllegalArgumentException("Mismatch between " + name + " and " + entryObject);
        }

        for (var entry : KeyArchetypeImpl.collectMethods(statement.argument(), entryObject).entrySet()) {
            if (entry.getValue() == null) {
                throw new IllegalArgumentException(
                    "Key component " + entry.getKey() + " has no method in " + entryObject);
            }
        }
        return new KeyArchetypeImpl(name, statement, entryObject);
    }
}
