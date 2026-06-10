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

public final class RuntimeGeneratedTypeBuilder extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder>
        implements GeneratedTypeBuilder {
    @NonNullByDefault
    public RuntimeGeneratedTypeBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public LegacyArchetype build() {
        return new GeneratedTypeImpl(this);
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
    protected RuntimeGeneratedTypeBuilder thisInstance() {
        return this;
    }

    private static final class GeneratedTypeImpl extends AbstractGeneratedType {
        GeneratedTypeImpl(final RuntimeGeneratedTypeBuilder builder) {
            super(builder);
        }
    }
}
