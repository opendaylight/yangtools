/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import static java.util.Objects.requireNonNull;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.codec.AbstractStringConverter;
import org.opendaylight.yangtools.yang.data.api.schema.stream.NormalizationException;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;
import org.opendaylight.yangtools.yang.model.api.type.RangeConstraint;

abstract sealed class Decimal64StringConverter extends AbstractStringConverter<Decimal64, DecimalTypeDefinition> {
    static final class Restricted extends Decimal64StringConverter {
        private final @NonNull RangeConstraint<Decimal64> rangeConstraint;

        Restricted(final DecimalTypeDefinition typeDef, final RangeConstraint<Decimal64> rangeConstraint) {
            super(typeDef);
            this.rangeConstraint = requireNonNull(rangeConstraint);
        }

        @Override
        void validate(final Decimal64 value) throws NormalizationException {
            final var ranges = rangeConstraint.getAllowedRanges();
            if (!ranges.contains(value)) {
                throw NormalizationException.ofConstraint("Value '" + value + "' is not in required ranges " + ranges,
                    ErrorType.APPLICATION, rangeConstraint);
            }
        }
    }

    static final class Unrestricted extends Decimal64StringConverter {
        Unrestricted(final DecimalTypeDefinition typeDef) {
            super(typeDef);
        }

        @Override
        void validate(final Decimal64 value) {
            // No-op
        }
    }

    private Decimal64StringConverter(final DecimalTypeDefinition typeDef) {
        super(Decimal64.class, typeDef);
    }

    @Override
    protected Decimal64 normalizeFromString(final DecimalTypeDefinition typedef, final String str)
            throws NormalizationException {
        final var parsed = Decimal64.valueOf(str);
        final Decimal64 value;
        try {
            value = parsed.scaleTo(typedef.getFractionDigits());
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Value '" + str + "' does not match required fraction-digits", e);
        }

        validate(value);
        return value;
    }

    @Override
    protected String canonizeToString(final DecimalTypeDefinition typedef, final Decimal64 obj)
            throws NormalizationException {
        validate(obj);
        return obj.toString();
    }

    abstract void validate(Decimal64 value) throws NormalizationException;
}
