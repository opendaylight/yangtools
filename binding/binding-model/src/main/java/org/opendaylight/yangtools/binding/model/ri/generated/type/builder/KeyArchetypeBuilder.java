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
import org.opendaylight.yangtools.binding.model.api.KeyArchetype;
import org.opendaylight.yangtools.yang.model.api.stmt.KeyEffectiveStatement;

/**
 * Builder for {@link KeyArchetype}.
 */
public abstract sealed class KeyArchetypeBuilder extends AbstractGeneratedTypeBuilder<KeyArchetype.Builder>
        implements KeyArchetype.Builder {
    public static final class Codegen extends KeyArchetypeBuilder {
        private String description;
        private String reference;
        private String moduleName;

        @NonNullByDefault
        public Codegen(final JavaTypeName typeName, final KeyEffectiveStatement statement,
                final JavaTypeName entryObject) {
            super(typeName, statement, entryObject);
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
        public KeyArchetype build() {
            return new CodegenKeyArchetype(typeName(), entryObject, statement,
                AbstractGeneratedType.toUnmodifiableProperties(getProperties()), description, reference, moduleName);
        }
    }

    public static final class Runtime extends KeyArchetypeBuilder {
        @NonNullByDefault
        public Runtime(final JavaTypeName typeName, final KeyEffectiveStatement statement,
                final JavaTypeName entryObject) {
            super(typeName, statement, entryObject);
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
        public KeyArchetype build() {
            return new RuntimeKeyArchetype(typeName(), entryObject, statement,
                AbstractGeneratedType.toUnmodifiableProperties(getProperties()));
        }
    }

    final @NonNull KeyEffectiveStatement statement;
    final @NonNull JavaTypeName entryObject;

    @NonNullByDefault
    KeyArchetypeBuilder(final JavaTypeName typeName, final KeyEffectiveStatement statement,
            final JavaTypeName entryObject) {
        super(typeName);
        this.statement = requireNonNull(statement);
        this.entryObject = requireNonNull(entryObject);
    }

    @Override
    protected final KeyArchetypeBuilder thisInstance() {
        return this;
    }
}
