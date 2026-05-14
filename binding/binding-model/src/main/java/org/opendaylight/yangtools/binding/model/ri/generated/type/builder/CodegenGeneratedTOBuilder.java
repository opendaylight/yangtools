/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.TypeObject;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.SerialVersionHelper;

// FIXME: package-private and abstract
public sealed class CodegenGeneratedTOBuilder extends AbstractGeneratedTOBuilder
        permits CodegenUnionTypeObjectArchetypeBuilder {
    private Restrictions restrictions;
    private String reference;
    private String description;
    private String moduleName;

    @NonNullByDefault
    CodegenGeneratedTOBuilder(final JavaTypeName typeName, final @Nullable Void unused) {
        super(typeName);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @NonNullByDefault
    public CodegenGeneratedTOBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public final void setRestrictions(final Restrictions restrictions) {
        this.restrictions = requireNonNull(restrictions);
    }

    @Override
    public final void setDescription(final String description) {
        this.description = requireNonNull(description);
    }

    @Override
    public final void setModuleName(final String moduleName) {
        this.moduleName = requireNonNull(moduleName);
    }

    @Override
    public final void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public GeneratedTransferObject<?> build() {
        return new GTO<>(this);
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    static sealed class GTO<T extends TypeObject> extends AbstractGeneratedTransferObject<T> permits CodegenUnionTO {
        private final @Nullable Restrictions restrictions;
        private final @Nullable String reference;
        private final @Nullable String description;
        private final @Nullable String moduleName;

        @Deprecated(since = "16.0.0", forRemoval = true)
        GTO(final CodegenGeneratedTOBuilder builder) {
            super(builder);
            restrictions = builder.restrictions;
            reference = builder.reference;
            description = builder.description;
            moduleName = builder.moduleName;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        public final Restrictions getRestrictions() {
            return restrictions;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        public final String getDescription() {
            return description;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        public final String getReference() {
            return reference;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        public final String getModuleName() {
            return moduleName;
        }

        @Deprecated(since = "16.0.0", forRemoval = true)
        @Override
        public final long serialVersionUID() {
            return SerialVersionHelper.computeSerialVersion(this);
        }
    }
}
