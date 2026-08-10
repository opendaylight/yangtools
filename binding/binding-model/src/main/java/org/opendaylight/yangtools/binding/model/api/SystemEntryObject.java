/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import java.util.List;
import java.util.Map;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.EntryObjectArchetype;
import org.opendaylight.yangtools.binding.model.ReturnType;
import org.opendaylight.yangtools.binding.model.Type;
import org.opendaylight.yangtools.binding.model.impl.ConcreteTypeImpl;
import org.opendaylight.yangtools.binding.model.impl.TypeMethods;

/**
 * {@link ParameterizedType} compatibility with {@link ReturnType} being a system-ordered {@link EntryObjectArchetype}.
 *
 * @param entryObject the {@link EntryObjectArchetype}
 * @since 16.0.0
 */
@Beta
@NonNullByDefault
@SuppressWarnings("removal")
public record SystemEntryObject(EntryObjectArchetype entryObject) implements ReturnTypeCompat {
    static final ConcreteTypeImpl MAP = new ConcreteTypeImpl(Map.class);

    public SystemEntryObject {
        requireNonNull(entryObject);
    }

    @Override
    public List<Type> getActualTypeArguments() {
        return List.of(TypeRef.of(entryObject.keyName()), entryObject);
    }

    @Override
    public ConcreteType getRawType() {
        return MAP;
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
