/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.IdentityArchetype;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;

/**
 * Builder for {@link IdentityArchetype}.
 */
public abstract sealed class IdentityArchetypeBuilder extends AbstractGeneratedTypeBuilder<IdentityArchetype.Builder>
        implements IdentityArchetype.Builder {
    public static final class Codegen extends IdentityArchetypeBuilder {
        private String description;
        private String reference;
        private String moduleName;

        @NonNullByDefault
        public Codegen(final JavaTypeName typeName) {
            super(typeName);
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
        public IdentityArchetype build() {
            return new CodegenIdentityArchetype();
            //(description, reference, moduleName);
        }
    }

    public static final class Runtime extends IdentityArchetypeBuilder {
        @NonNullByDefault
        public Runtime(final JavaTypeName typeName) {
            super(typeName);
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
        public IdentityArchetype build() {
            return new RuntimeIdentityArchetype();
        }
    }

    @NonNullByDefault
    IdentityArchetypeBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    protected final IdentityArchetype.Builder thisInstance() {
        return this;
    }

//    final @NonNull IdentityArchetype build(final @Nullable String description, final @Nullable String reference,
//            final @Nullable String moduleName) {
//        return new RuntimeIdentityArchetype(typeName(), getImplementsTypes(),
//            AbstractGeneratedType.toUnmodifiableMethods(getMethodDefinitions()),
//            List.copyOf(getEnclosedTransferObjects()), getEnumerations(), getYangSourceDefinition().orElse(null),
//            getComment(), description, reference, moduleName);
//    }
}
