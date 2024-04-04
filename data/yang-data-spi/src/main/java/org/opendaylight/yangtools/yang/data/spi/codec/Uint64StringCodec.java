/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.common.Uint64;
import org.opendaylight.yangtools.yang.data.api.codec.Uint64Codec;
import org.opendaylight.yangtools.yang.model.api.type.Uint64TypeDefinition;

final class Uint64StringCodec extends AbstractIntegerStringCodec<Uint64, Uint64TypeDefinition> implements
        Uint64Codec<String> {
    Uint64StringCodec(final Uint64TypeDefinition typeDef) {
        super(Uint64.class, typeDef);
    }

    @Override
    protected Uint64 deserialize(final String stringRepresentation, final int base) {
        return Uint64.valueOf(stringRepresentation, base);
    }
}
