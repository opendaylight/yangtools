/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import static java.util.Objects.requireNonNull;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

@NonNullByDefault
record RestrictedDecimal64TypeImpl(Decimal64Type type, Restrictions restrictions)
        implements RestrictedDecimal64Type {
    RestrictedDecimal64TypeImpl {
        requireNonNull(type);
        requireNonNull(restrictions);
    }

    @Override
    public int fractionDigits() {
        return type.fractionDigits();
    }

    @Override
    public JavaTypeName name() {
        return type.name();
    }

    @Override
    public RestrictedDecimal64Type withRestrictions(final Restrictions newRestrictions) {
        return restrictions.equals(newRestrictions) ? this : new RestrictedDecimal64TypeImpl(this, newRestrictions);
    }

    @Override
    public Decimal64Type withoutRestrictions() {
        return type;
    }

    @Override
    public int hashCode() {
        return TypeMethods.hashCode(this);
    }

    @Override
    public boolean equals(final @Nullable Object obj) {
        return TypeMethods.equals(this, obj);
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(RestrictedType.class)
            .add("name", name())
            .add("fractionDigits", fractionDigits())
            .toString();
    }
}
