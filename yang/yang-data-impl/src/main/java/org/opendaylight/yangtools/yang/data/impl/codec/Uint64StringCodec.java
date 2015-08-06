/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.impl.codec;

import com.google.common.base.Optional;
import java.math.BigInteger;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.model.api.type.UnsignedIntegerTypeDefinition;

class Uint64StringCodec extends AbstractIntegerStringCodec<BigInteger, UnsignedIntegerTypeDefinition> implements
        Uint64Codec<String> {

    protected Uint64StringCodec(final Optional<UnsignedIntegerTypeDefinition> typeDef) {
        super(typeDef, extractRange(typeDef.orNull()), BigInteger.class);
    }

    @Override
    public final BigInteger deserialize(final String stringRepresentation, final int base) {
        return new BigInteger(stringRepresentation, base);
    }

    @Override
    public final String serialize(final BigInteger data) {
        return data == null ? "" : data.toString();
    }

    @Override
    protected BigInteger convertValue(final Number value) {
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        return BigInteger.valueOf(value.longValue());
    }
}
