/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static com.google.common.base.Verify.verify;
import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.EntryObject;
import org.opendaylight.yangtools.binding.Key;
import org.opendaylight.yangtools.binding.contract.Naming;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;

/**
 * An archetype for a {@link Key} attached to an {@link EntryObject}.
 *
 * @param name this type's {@link JavaTypeName}}
 * @param entryObject the {@link JavaTypeName}} of the corresponding {@link EntryObject}
 * @param statement the {@link KeyEffectiveStatement}
 * @param fields {@link Type}s in the same order as {@code statement().argument()}
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
public record KeyArchetype(
        JavaTypeName name,
        KeyEffectiveStatement statement,
        EntryObjectArchetype entryObject,
        List<Type> fields) implements Archetype {
    public KeyArchetype {
        requireNonNull(name);
        requireNonNull(statement);
        requireNonNull(entryObject);
        fields = List.copyOf(fields);
        verify(fields.size() == statement.argument().size());
    }

    public List<GeneratedProperty> getProperties() {
        final var arg = statement().argument();
        final var props = new ArrayList<GeneratedProperty>(arg.size());
        final var kit = arg.iterator();

        for (var field : fields()) {
            props.add(new GeneratedPropertyImpl(Naming.getPropertyName(kit.next().getLocalName()), field, true));
        }

        return props;
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }
}
