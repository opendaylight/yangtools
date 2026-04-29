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
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.YangDataArchetype;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.FeatureEffectiveStatement;

/**
 * Builder for {@link YangDataArchetype}.
 */
public abstract sealed class YangDataArchetypeBuilder extends AbstractGeneratedTypeBuilder<YangDataArchetype.Builder>
        implements YangDataArchetype.Builder {
    public static final class Codegen extends YangDataArchetypeBuilder {
        private String description;
        private String reference;
        private String moduleName;

        @NonNullByDefault
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
        public YangDataArchetype build() {
            return new CodegenYangDataArchetype(typeName(), tatement,
                getYangSourceDefinition().orElse(null), getComment(), description, reference, moduleName);
        }
    }

    public static final class Runtime extends YangDataArchetypeBuilder {
        @NonNullByDefault
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
        public YangDataArchetype build() {
            return new RuntimeYangDataArchetype(typeName(), statement,
                getYangSourceDefinition().orElse(null), getComment());
        }
    }

    final @NonNull YangDataEffectiveStatement statement;

    @NonNullByDefault
    YangDataArchetypeBuilder(final JavaTypeName typeName, final YangDataEffectiveStatement statement) {
        super(typeName);
        this.statement = requireNonNull(statement);
    }

    @Override
    protected final YangDataArchetype.Builder thisInstance() {
        return this;
    }
}
