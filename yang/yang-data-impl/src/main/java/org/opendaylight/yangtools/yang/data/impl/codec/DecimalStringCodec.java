/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import java.math.BigDecimal;
import org.opendaylight.yangtools.yang.data.api.codec.DecimalCodec;
import org.opendaylight.yangtools.yang.model.api.type.DecimalTypeDefinition;

class DecimalStringCodec extends TypeDefinitionAwareCodec<BigDecimal, DecimalTypeDefinition>
        implements DecimalCodec<String> {

    protected DecimalStringCodec(final Optional<DecimalTypeDefinition> typeDef) {
        super(typeDef, BigDecimal.class);
    }

    static TypeDefinitionAwareCodec<?,DecimalTypeDefinition> from(final DecimalTypeDefinition normalizedType) {
        return new DecimalStringCodec(Optional.fromNullable(normalizedType));
    }

    @Override
    public final String serialize(final BigDecimal data) {
        return data == null ? "" : data.toString();
    }

    @Override
    public final BigDecimal deserialize(final String stringRepresentation) {
        Preconditions.checkArgument( stringRepresentation != null , "Input cannot be null" );
        return new BigDecimal(stringRepresentation);
    }
}