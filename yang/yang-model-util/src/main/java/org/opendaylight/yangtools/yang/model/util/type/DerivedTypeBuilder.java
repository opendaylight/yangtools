/*
 * Copyright (c) 2015 Pantheon Technologies s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.model.util.type;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

import java.util.Optional;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Builder of {@link TypeDefinition}s for use in typedef statements.
 *
 * @param <T> Resulting {@link TypeDefinition}
 */
public abstract class DerivedTypeBuilder<T extends TypeDefinition<T>> extends TypeBuilder<T> {
    private static final Logger LOG = LoggerFactory.getLogger(DerivedTypeBuilder.class);
    private Object defaultValue;
    private String description;
    private String reference;
    private Status status = Status.CURRENT;
    private String units;

    DerivedTypeBuilder(final T baseType, final SchemaPath path) {
        super(requireNonNull(baseType), path);

        checkArgument(baseType instanceof AbstractBaseType || baseType instanceof AbstractDerivedType
            || baseType instanceof AbstractRestrictedType,
            "Derived type can be built only from a base, derived, or restricted type, not %s", baseType);

        // http://tools.ietf.org/html/rfc6020#section-7.3.4
        defaultValue = baseType.getDefaultValue().orElse(null);

        // In similar vein, it makes sense to propagate units
        units = baseType.getUnits().orElse(null);
    }

    public void setDefaultValue(final @NonNull Object defaultValue) {
        this.defaultValue = requireNonNull(defaultValue);
    }

    public final void setDescription(final @NonNull String description) {
        this.description = requireNonNull(description);
    }

    public final void setReference(final @NonNull String reference) {
        this.reference = requireNonNull(reference);
    }

    public final void setStatus(final @NonNull Status status) {
        this.status = requireNonNull(status);
    }

    public final void setUnits(final String units) {
        requireNonNull(units);

        final Optional<String> baseUnits = getBaseType().getUnits();
        if (baseUnits.isPresent() && !units.equals(baseUnits.get())) {
            LOG.warn("Type {} uverrides 'units' of type {} to \"{}\"", getPath(), getBaseType(), units);
        }

        this.units = units;
    }

    final Object getDefaultValue() {
        return defaultValue;
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
