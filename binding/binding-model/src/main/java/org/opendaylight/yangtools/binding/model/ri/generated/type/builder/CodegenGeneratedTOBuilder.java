/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import static java.util.Objects.requireNonNull;

import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;

// FIXME: final
public non-sealed class CodegenGeneratedTOBuilder extends AbstractGeneratedTOBuilder {
    private Restrictions restrictions;
    private GeneratedPropertyBuilder suid;
    private String reference;
    private String description;
    private String moduleName;

    public CodegenGeneratedTOBuilder(final JavaTypeName typeName) {
        super(typeName);
    }

    @Override
    public final void setRestrictions(final Restrictions restrictions) {
        this.restrictions = requireNonNull(restrictions);
    }

    @Override
    public final void setSUID(final GeneratedPropertyBuilder newSuid) {
        suid = requireNonNull(newSuid);
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
    public GeneratedTransferObject build() {
        return new GTO(this);
    }

    protected static class GTO extends AbstractGeneratedTransferObject {
        private final Restrictions restrictions;
        private final GeneratedProperty suid;
        private final String reference;
        private final String description;
        private final String moduleName;

        protected GTO(final CodegenGeneratedTOBuilder builder) {
            super(builder);
            restrictions = builder.restrictions;
            reference = builder.reference;
            description = builder.description;
            moduleName = builder.moduleName;

            if (builder.suid == null) {
                suid = null;
            } else {
                suid = builder.suid.toInstance();
            }
        }

        @Override
        public final Restrictions getRestrictions() {
            return restrictions;
        }

        @Override
        public final GeneratedProperty getSUID() {
            return suid;
        }

        @Override
        public final String getDescription() {
            return description;
        }

        @Override
        public final String getReference() {
            return reference;
        }

        @Override
        public final String getModuleName() {
            return moduleName;
        }
    }
}
