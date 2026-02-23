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
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.UnionTypeObjectArchetype;

public final class RuntimeUnionTypeObjectArchetypeBuilder extends RuntimeGeneratedTOBuilder
        implements UnionTypeObjectArchetype.Builder {
    private List<String> typePropertyNames = List.of();

    public RuntimeUnionTypeObjectArchetypeBuilder(final JavaTypeName typeName) {
        super(typeName);
        setIsUnion(true);
    }

    @Override
    public void setTypePropertyNames(final List<String> propertyNames) {
        typePropertyNames = List.copyOf(propertyNames);
    }

    @Override
    public UnionTypeObjectArchetype build() {
        return new UnionGTO(this, typePropertyNames);
    }

    private static final class UnionGTO extends GTO implements UnionTypeObjectArchetype {
        private final @NonNull List<String> typePropertyNames;

        UnionGTO(final RuntimeGeneratedTOBuilder builder, final List<String> typePropertyNames) {
            super(builder);
            this.typePropertyNames = requireNonNull(typePropertyNames);
        }

        @Override
        public List<String> typePropertyNames() {
            return typePropertyNames;
        }
    }
}