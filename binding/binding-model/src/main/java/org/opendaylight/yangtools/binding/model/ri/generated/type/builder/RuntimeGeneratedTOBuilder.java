/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;

abstract sealed class RuntimeGeneratedTOBuilder extends AbstractGeneratedTOBuilder
        permits RuntimeBitsTypeObjectArchetypeBuilder, RuntimeScalarTypeObjectArchetypeBuilder,
                RuntimeUnionTypeObjectArchetypeBuilder {
    @NonNullByDefault
    RuntimeGeneratedTOBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public final void setRestrictions(final Restrictions restrictions) {
        // No-op
    }

    @Override
    public final void setSUID(final GeneratedPropertyBuilder suid) {
        // No-op
    }

    @Override
    public final void setDescription(final String description) {
        // No-op
    }

    @Override
    public final void setModuleName(final String moduleName) {
        // No-op
    }

    @Override
    public final void setReference(final String reference) {
        // No-op
    }

    abstract static class GTO extends AbstractGeneratedTransferObject {
        GTO(final RuntimeGeneratedTOBuilder builder) {
            super(builder);
        }

        @Override
        public final Restrictions getRestrictions() {
            throw unsupported();
        }

        @Override
        public final GeneratedProperty getSUID() {
            throw unsupported();
        }

        @Override
        public final String getDescription() {
            throw unsupported();
        }

        @Override
        public final String getReference() {
            throw unsupported();
        }

        @Override
        public final String getModuleName() {
            throw unsupported();
        }

        private static UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Not available at runtime");
        }
    }
}
