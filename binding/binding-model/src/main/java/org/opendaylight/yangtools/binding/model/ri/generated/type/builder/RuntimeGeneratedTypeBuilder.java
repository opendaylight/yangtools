/*
 * Copyright (c) 2018 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedTypeBuilder;

public final class RuntimeGeneratedTypeBuilder extends AbstractGeneratedTypeBuilder<GeneratedTypeBuilder> implements
        GeneratedTypeBuilder {

    public RuntimeGeneratedTypeBuilder(final Archetype<?> archetype) {
        super(archetype);
        setAbstract(true);
    }

    @Override
    public GeneratedType build() {
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

        @Override
        public String getDescription() {
            throw unsupported();
        }

        @Override
        public String getReference() {
            throw unsupported();
        }

        @Override
        public String getModuleName() {
            throw unsupported();
        }

        private static UnsupportedOperationException unsupported() {
            return new UnsupportedOperationException("Not available at runtime");
        }
    }
}
