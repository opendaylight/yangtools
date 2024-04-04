/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import com.google.common.annotations.Beta;
import org.eclipse.jdt.annotation.NonNull;
import org.opendaylight.yangtools.yang.common.Decimal64;
import org.opendaylight.yangtools.yang.common.ErrorType;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.data.api.codec.YangInvalidValueException;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;

/**
 * Do not use this class outside of yangtools, its presence does not fall into the API stability contract.
 */
@Beta
public final class DecimalStringCodec extends TypeDefinitionAwareCodec<Decimal64, DecimalTypeDefinition>
        implements DecimalCodec<String> {
    private DecimalStringCodec(final DecimalTypeDefinition typeDef) {
        super(Decimal64.class, typeDef);
    }

    public static @NonNull DecimalStringCodec from(final DecimalTypeDefinition type) {
        return new DecimalStringCodec(type);
    }

    @Override
    protected Decimal64 deserializeImpl(final String product) {
        final var typeDef = typeDefinition();
        final var parsed = Decimal64.valueOf(product);
        final Decimal64 value;
        try {
            value = parsed.scaleTo(typeDef.getFractionDigits());
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("Value '" + product + "' does not match required fraction-digits", e);
        }

        typeDef.getRangeConstraint().ifPresent(constraint -> {
            final var ranges = constraint.getAllowedRanges();
            if (!ranges.contains(value)) {
                throw new YangInvalidValueException(ErrorType.APPLICATION, constraint,
                        "Value '" + value + "' is not in required ranges " + ranges);
            }
        });
        return value;
    }

    @Override
    protected String serializeImpl(final Decimal64 input) {
        return input.toString();
    }
}
