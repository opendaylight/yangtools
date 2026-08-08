/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.impl;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.opendaylight.yangtools.binding.model.TypeName;
import org.opendaylight.yangtools.binding.model.api.Decimal64Type;
import org.opendaylight.yangtools.binding.model.api.RestrictedDecimal64Type;
import org.opendaylight.yangtools.binding.model.api.Restrictions;
import org.opendaylight.yangtools.yang.common.Decimal64;

@NonNullByDefault
public record Decimal64TypeImpl(int fractionDigits) implements Decimal64Type {
    private static final TypeName NAME = TypeName.ofClass(Decimal64.class);

    private static final @NonNull Decimal64Type[] INSTANCES;

    static {
        final var tmp = new @NonNull Decimal64Type[18];
        for (int i = 0; i < tmp.length; ++i) {
            tmp[i] = new Decimal64TypeImpl(i + 1);
        }
        INSTANCES = tmp;
    }

    public Decimal64TypeImpl {
        if (fractionDigits < 1 || fractionDigits > 18) {
            throw new IllegalArgumentException("invalid fractionDigits");
        }
    }

    public static Decimal64Type of(final int fractionDigits) {
        try {
            return Decimal64TypeImpl.INSTANCES[fractionDigits - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid fractionDigits " + fractionDigits, e);
        }
    }

    @Override
    public TypeName name() {
        return NAME;
    }

    @Override
    public RestrictedDecimal64Type withRestrictions(final Restrictions newRestrictions) {
        return new RestrictedDecimal64TypeImpl(this, newRestrictions);
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
    public String toString() {
        return TypeMethods.toStringHelper(this).add("fractionDigits", fractionDigits).toString();
    }
}
