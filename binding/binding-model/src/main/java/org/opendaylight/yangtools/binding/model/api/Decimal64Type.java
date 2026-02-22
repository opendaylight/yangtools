/*
 * Copyright (c) 2026 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.yang.common.Decimal64;

/**
 * A {@link ConcreteType} representing {@link Decimal64}.
 */
@NonNullByDefault
public sealed interface Decimal64Type extends ConcreteType permits DefaultDecimal64Type, RestrictedDecimal64Type {
    @Override
    RestrictedDecimal64Type withRestrictions(Restrictions newRestrictions);

    /**
     * {@return the {@code fraction-digits} this type represents}
     */
    int fractionDigits();

    /**
     * {@return a {@link Decimal64Type} for specified fraction digits}
     * @param fractionDigits the fraction digits
     * @throws IllegalArgumentException when {@code fractionDigits} is not in range [1..18]
     */
    static Decimal64Type ofFractionDigits(final int fractionDigits) {
        try {
            return DefaultDecimal64Type.INSTANCES[fractionDigits - 1];
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException("Invalid fractionDigits " + fractionDigits, e);
        }
    }
}
