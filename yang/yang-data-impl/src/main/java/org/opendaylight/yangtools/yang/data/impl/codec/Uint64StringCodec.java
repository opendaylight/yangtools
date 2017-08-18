/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Optional;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

final class Uint64StringCodec extends AbstractIntegerStringCodec<BigInteger, UnsignedIntegerTypeDefinition> implements
        Uint64Codec<String> {

    Uint64StringCodec(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
        super(typeDef, extractRange(typeDef.orElse(null)), BigInteger.class);
    }

    @Override
    BigInteger deserialize(final String stringRepresentation, final int base) {
        return new BigInteger(stringRepresentation, base);
    }

    @Override
    public String serialize(final BigInteger data) {
        return Objects.toString(data, "");
    }

    @Override
    BigInteger convertValue(final Number value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        return BigInteger.valueOf(value.longValue());
    }
}
