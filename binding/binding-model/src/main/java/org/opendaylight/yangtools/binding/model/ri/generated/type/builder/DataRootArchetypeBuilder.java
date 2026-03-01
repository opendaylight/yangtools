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
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype;
import org.opendaylight.yangtools.binding.model.api.DataRootArchetype.Builder;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.TypeRef;
import org.opendaylight.yangtools.binding.model.ri.BindingTypes;

/**
 * Builder for {@link DataRootArchetype}.
 */
public abstract sealed class DataRootArchetypeBuilder extends AbstractGeneratedTypeBuilder<DataRootArchetype.Builder>
        implements DataRootArchetype.Builder {
    public static final class Codegen extends DataRootArchetypeBuilder {
        private String description;
        private String reference;
        private String moduleName;

        @NonNullByDefault
        public Codegen(final JavaTypeName typeName, final JavaTypeName yangModuleInfo) {
            super(typeName, yangModuleInfo);
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
        public DataRootArchetype build() {
            return build(description, reference, moduleName);
        }
    }

    public static final class Runtime extends DataRootArchetypeBuilder {
        @NonNullByDefault
        public Runtime(final JavaTypeName typeName, final JavaTypeName yangModuleInfo) {
            super(typeName, yangModuleInfo);
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
        public DataRootArchetype build() {
            return build(null, null, null);
        }
    }

    private final @NonNull JavaTypeName yangModuleInfo;

    @NonNullByDefault
    DataRootArchetypeBuilder(final JavaTypeName typeName, final JavaTypeName yangModuleInfo) {
        super(typeName);
        this.yangModuleInfo = requireNonNull(yangModuleInfo);
        addImplementsType(BindingTypes.dataRoot(TypeRef.of(typeName)));
    }

    @Override
    protected final Builder thisInstance() {
        return this;
    }

    final @NonNull DataRootArchetype build(final @Nullable String description, final @Nullable String reference,
            final @Nullable String moduleName) {
        return new DataRootArchetypeImpl(typeName(), yangModuleInfo, getImplementsTypes(),
            AbstractGeneratedType.toUnmodifiableMethods(getMethodDefinitions()),
            List.copyOf(getEnclosedTransferObjects()), getEnumerations(), getYangSourceDefinition().orElse(null),
            getComment(), description, reference, moduleName);
    }
}
