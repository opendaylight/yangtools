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
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;

/**
 * A run-time implementation of {@link KeyArchetype}.
 */
record RuntimeKeyArchetype(
        @NonNull JavaTypeName name,
        @NonNull JavaTypeName entryObject,
        @NonNull KeyEffectiveStatement statement,
        @NonNull List<GeneratedProperty> getProperties) implements KeyArchetype {
    RuntimeKeyArchetype {
        requireNonNull(name);
        requireNonNull(entryObject);
        requireNonNull(statement);
        getProperties = List.copyOf(getProperties);
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public String getReference() {
        return null;
    }

    @Override
    public String getModuleName() {
        return null;
    }
}
