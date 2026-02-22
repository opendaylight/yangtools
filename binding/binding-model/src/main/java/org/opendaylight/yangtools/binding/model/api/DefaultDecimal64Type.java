/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import com.google.common.base.MoreObjects;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.yang.common.Decimal64;

@NonNullByDefault
record DefaultDecimal64Type(int fractionDigits) implements Decimal64Type {
    private static final JavaTypeName NAME = JavaTypeName.create(Decimal64.class);

    static final @NonNull Decimal64Type[] INSTANCES;

    static {
        final var tmp = new Decimal64Type[18];
        for (int i = 0; i < tmp.length; ++i) {
            tmp[i] = new DefaultDecimal64Type(i + 1);
        }
        INSTANCES = tmp;
    }

    DefaultDecimal64Type {
        if (fractionDigits < 1 || fractionDigits > 18) {
            throw new IllegalArgumentException("invalid fractionDigits");
        }
    }

    @Override
    public JavaTypeName name() {
        return NAME;
    }

    @Override
    public RestrictedDecimal64Type withRestrictions(final Restrictions newRestrictions) {
        return new DefaultRestrictedDecimal64Type(this, newRestrictions);
    }

    @Override
    public final int hashCode() {
        return NAME.hashCode();
    }

    @Override
    public final boolean equals(final @Nullable Object obj) {
        return this == obj || obj instanceof Type other && NAME.equals(other.name());
    }

    @Override
    public final String toString() {
        return MoreObjects.toStringHelper(ConcreteType.class)
            .add("name", NAME)
            .add("fractionDigits", fractionDigits)
            .toString();
    }
}
