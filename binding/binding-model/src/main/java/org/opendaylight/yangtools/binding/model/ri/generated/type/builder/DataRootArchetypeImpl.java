/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import java.util.List;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.binding.model.api.Archetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.MethodSignature;
import org.opendaylight.yangtools.binding.model.api.Type;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * The default implementation {@link DataRootArchetype}.
 * @since 15.0.0
 */
record DataRootArchetypeImpl(
        @NonNull JavaTypeName name,
        @NonNull ModuleEffectiveStatement statement,
        @NonNull List<Type> getImplements,
        @NonNull List<MethodSignature> getMethodDefinitions,
        @NonNull List<@NonNull Archetype> enclosedTypes) implements DataRootArchetype {
    DataRootArchetypeImpl {
        requireNonNull(name);
        requireNonNull(statement);
        getImplements = List.copyOf(getImplements);
        getMethodDefinitions = List.copyOf(getMethodDefinitions);
        enclosedTypes = List.copyOf(enclosedTypes);
    }
}
