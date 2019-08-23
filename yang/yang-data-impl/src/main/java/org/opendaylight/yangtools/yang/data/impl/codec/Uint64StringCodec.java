/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.impl.codec;

import java.math.BigInteger;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;

final class Uint64StringCodec extends AbstractIntegerStringCodec<BigInteger, Uint64TypeDefinition> implements
        Uint64Codec<String> {
    Uint64StringCodec(final Uint64TypeDefinition typeDef) {
        super(typeDef, extractRange(typeDef), BigInteger.class);
    }

    @Override
    protected BigInteger deserialize(final String stringRepresentation, final int base) {
        return new BigInteger(stringRepresentation, base);
    }

    @Override
    protected String serializeImpl(final BigInteger input) {
        return input.toString();
    }
}
