/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.ri.generated.type.builder;

import org.opendaylight.yangtools.binding.model.Archetype;
import org.opendaylight.yangtools.binding.model.api.GeneratedProperty;
import org.opendaylight.yangtools.binding.model.api.GeneratedTransferObject;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.binding.model.api.type.builder.GeneratedPropertyBuilder;

public class CodegenGeneratedTOBuilder extends AbstractGeneratedTOBuilder {
    private Restrictions restrictions;
    private GeneratedPropertyBuilder suid;
    private String reference;
    private String description;
    private String moduleName;

    public CodegenGeneratedTOBuilder(final Archetype<?> archetype) {
        super(archetype);
    }

    @Override
    public final void setRestrictions(final Restrictions restrictions) {
        this.restrictions = restrictions;
    }

    @Override
    public final void setSUID(final GeneratedPropertyBuilder newSuid) {
        suid = newSuid;
    }

    @Override
    public final void setDescription(final String description) {
        this.description = description;
    }

    @Override
    public final void setModuleName(final String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public final void setReference(final String reference) {
        this.reference = reference;
    }

    @Override
    public final GeneratedTransferObject build() {
        return new GTO(this);
    }

    private static final class GTO extends AbstractGeneratedTransferObject {
        private final Restrictions restrictions;
        private final GeneratedProperty suid;
        private final String reference;
        private final String description;
        private final String moduleName;

        GTO(final CodegenGeneratedTOBuilder builder) {
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
        public Restrictions getRestrictions() {
            return restrictions;
        }

        @Override
        public GeneratedProperty getSUID() {
            return suid;
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
}
