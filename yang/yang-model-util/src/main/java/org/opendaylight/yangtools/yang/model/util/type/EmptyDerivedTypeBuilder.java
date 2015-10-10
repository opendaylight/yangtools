/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.type.EmptyTypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EmptyDerivedTypeBuilder extends DerivedTypeBuilder<EmptyTypeDefinition> {
    private static final Logger LOG = LoggerFactory.getLogger(EmptyDerivedTypeBuilder.class);

    EmptyDerivedTypeBuilder(final EmptyTypeDefinition baseType, final SchemaPath path) {
        super(baseType, path);
    }

    @Override
    public void setDefaultValue(@Nonnull final Object defaultValue) {
        LOG.warn("Attempted to set default value for empty type {}", getPath());
    }

    @Override
    public EmptyDerivedType build() {
        return new EmptyDerivedType(getBaseType(), getPath(), getDefaultValue(), getDescription(), getReference(),
            getStatus(), getUnits());
    }
}
