/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import com.google.common.base.Preconditions;
import javax.annotation.Nonnull;

import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder of {@link TypeDefinitions} for use in typedef statements.
 *
 * @param <T> Resulting {@link TypeDefinition}
 */
public abstract class DerivedTypeBuilder<T extends TypeDefinition<T>> extends TypeBuilder<T> {
    private static final Logger LOG = LoggerFactory.getLogger(DecimalTypeBuilder.class);
    private Object defaultValue;
    private QNameModule defaultValueModule;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    private String units;

    DerivedTypeBuilder(final T baseType, final SchemaPath path) {
        super(Preconditions.checkNotNull(baseType), path);

        Preconditions.checkArgument(baseType instanceof AbstractBaseType
                || baseType instanceof AbstractDerivedType || baseType instanceof AbstractRestrictedType,
            "Derived type can be built only from a base, derived, or restricted type, not %s", baseType);

        // http://tools.ietf.org/html/rfc6020#section-7.3.4
        defaultValue = baseType.getDefaultValue();

        // In similar vein, it makes sense to propagate units
        units = baseType.getUnits();
    }

    public void setDefaultValue(@Nonnull final Object defaultValue) {
        this.defaultValue = Preconditions.checkNotNull(defaultValue);
    }

    public void setDefaultValueModule(@Nonnull final QNameModule defaultValueModule) {
        this.defaultValueModule = Preconditions.checkNotNull(defaultValueModule);
    }

    public final void setDescription(@Nonnull final String description) {
        this.description = Preconditions.checkNotNull(description);
    }

    public final void setReference(@Nonnull final String reference) {
        this.reference = Preconditions.checkNotNull(reference);
    }

    public final void setStatus(@Nonnull final Status status) {
        this.status = Preconditions.checkNotNull(status);
    }

    public final void setUnits(final String units) {
        Preconditions.checkNotNull(units);
        if (getBaseType().getUnits() != null && !units.equals(getBaseType().getUnits())) {
            LOG.warn("Type {} uverrides 'units' of type {} to \"{}\"", getPath(), getBaseType(), units);
        }

        this.units = units;
    }

    final Object getDefaultValue() {
        return defaultValue;
    }

    final QNameModule getDefaultValueModule() {
        return defaultValueModule;
    }

    final String getDescription() {
        return description;
    }

    final String getReference() {
        return reference;
    }

    final Status getStatus() {
        return status;
    }

    final String getUnits() {
        return units;
    }
}
