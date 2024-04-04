/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.common.Uint8;
import org.opendaylight.yangtools.yang.data.api.codec.Uint8Codec;
import org.opendaylight.yangtools.yang.model.api.type.Uint8TypeDefinition;

final class Uint8StringCodec extends AbstractIntegerStringCodec<Uint8, Uint8TypeDefinition> implements
        Uint8Codec<String> {
    Uint8StringCodec(final Uint8TypeDefinition typeDef) {
        super(Uint8.class, typeDef);
    }

    @Override
    protected Uint8 deserialize(final String stringRepresentation, final int base) {
        return Uint8.valueOf(stringRepresentation, base);
    }
}
