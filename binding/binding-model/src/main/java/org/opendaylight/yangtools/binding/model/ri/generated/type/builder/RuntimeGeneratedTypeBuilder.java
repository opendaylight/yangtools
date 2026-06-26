/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.LegacyArchetype;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;
import org.opendaylight.yangtools.yang.model.api.meta.EffectiveStatement;

public final class RuntimeGeneratedTypeBuilder<S extends EffectiveStatement<?, ?>>
        extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder<S>, S> implements GeneratedTypeBuilder<S> {
    private static final class RuntimeLegacyArchetype<S extends EffectiveStatement<?, ?>>
            extends AbstractGeneratedType<S> {
        @NonNullByDefault
        RuntimeLegacyArchetype(final RuntimeGeneratedTypeBuilder<S> builder) {
            super(builder);
        }
    }

    @NonNullByDefault
    public RuntimeGeneratedTypeBuilder(final JavaTypeName typeName, final S statement) {
        super(typeName, statement);
    }

    @Override
    public LegacyArchetype<S> build() {
        return new RuntimeLegacyArchetype<>(this);
    }

    @Override
    public void setDescription(final String description) {
        // No-op
    }

    @Override
    public void setModuleName(final String moduleName) {
        // No-op
    }

    @Override
    public void setReference(final String reference) {
        // No-op
    }

    @Override
    protected RuntimeGeneratedTypeBuilder<S> thisInstance() {
        return this;
    }
}
