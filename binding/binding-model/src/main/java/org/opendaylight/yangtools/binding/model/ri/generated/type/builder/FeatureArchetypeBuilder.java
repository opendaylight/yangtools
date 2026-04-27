/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.FeatureArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

/**
 * Builder for {@link FeatureArchetype}.
 */
public abstract sealed class FeatureArchetypeBuilder extends AbstractGeneratedTypeBuilder<FeatureArchetype.Builder>
        implements FeatureArchetype.Builder {
    @NonNullByDefault
    public static final class Codegen extends FeatureArchetypeBuilder {
        private String description;
        private String reference;
        private String moduleName;

        public Codegen(final JavaTypeName typeName, final FeatureEffectiveStatement statement,
                final JavaTypeName dataRoot) {
            super(typeName, statement, dataRoot);
        }

        @Override
        public void setDescription(final String description) {
            this.description = requireNonNull(description);
        }

        @Override
        public void setModuleName(final String moduleName) {
            this.moduleName = requireNonNull(moduleName);
        }

        @Override
        public void setReference(final String reference) {
            this.reference = requireNonNull(reference);
        }

        @Override
        public FeatureArchetype build() {
            return new CodegenFeatureArchetype(typeName(), dataRoot, statement,
                AbstractGeneratedType.toUnmodifiableAnnotations(getAnnotations()), getConstants(),
                getYangSourceDefinition().orElse(null), getComment(), description, reference, moduleName);
        }
    }

    public static final class Runtime extends FeatureArchetypeBuilder {
        public Runtime(final JavaTypeName typeName, final FeatureEffectiveStatement statement,
                final JavaTypeName dataRoot) {
            super(typeName, statement, dataRoot);
        }

        @Override
        public void setDescription(final String description) {
            // no-op
        }

        @Override
        public void setModuleName(final String moduleName) {
            // no-op
        }

        @Override
        public void setReference(final String reference) {
            // no-op
        }

        @Override
        public FeatureArchetype build() {
            return new RuntimeFeatureArchetype(typeName(), dataRoot, statement,
                AbstractGeneratedType.toUnmodifiableAnnotations(getAnnotations()), getConstants(),
                getYangSourceDefinition().orElse(null), getComment());
        }
    }

    final @NonNull FeatureEffectiveStatement statement;
    final @NonNull JavaTypeName dataRoot;

    @NonNullByDefault
    FeatureArchetypeBuilder(final JavaTypeName typeName, final FeatureEffectiveStatement statement,
            final JavaTypeName dataRoot) {
        super(typeName);
        this.statement = requireNonNull(statement);
        this.dataRoot = requireNonNull(dataRoot);
    }

    @Override
    protected final FeatureArchetype.Builder thisInstance() {
        return this;
    }
}
