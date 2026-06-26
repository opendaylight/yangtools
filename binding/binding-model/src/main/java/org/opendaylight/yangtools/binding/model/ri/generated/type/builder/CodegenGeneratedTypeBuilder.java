/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
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

public final class CodegenGeneratedTypeBuilder<S extends EffectiveStatement<?, ?>>
        extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder<S>, S> implements GeneratedTypeBuilder<S> {
    private static final class CodegenLegacyArchetype<S extends EffectiveStatement<?, ?>>
            extends AbstractGeneratedType<S> {
        private final String description;
        private final String reference;
        private final String moduleName;

        CodegenLegacyArchetype(final CodegenGeneratedTypeBuilder<S> builder) {
            super(builder);

            description = builder.description;
            reference = builder.reference;
            moduleName = builder.moduleName;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getReference() {
            return reference;
        }

        @Override
        public String getModuleName() {
            return moduleName;
        }
    }

    private String description;
    private String reference;
    private String moduleName;

    @NonNullByDefault
    public CodegenGeneratedTypeBuilder(final JavaTypeName typeName, final S statement) {
        super(typeName, statement);
    }

    @Override
    public void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public LegacyArchetype<S> build() {
        return new CodegenLegacyArchetype<>(this);
    }

    @Override
    protected CodegenGeneratedTypeBuilder<S> thisInstance() {
        return this;
    }
}
