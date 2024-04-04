/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.common.Uint16;
import org.opendaylight.yangtools.yang.data.api.codec.Uint16Codec;
import org.opendaylight.yangtools.yang.model.api.type.Uint16TypeDefinition;

final class Uint16StringCodec extends AbstractIntegerStringCodec<Uint16, Uint16TypeDefinition> implements
        Uint16Codec<String> {
    Uint16StringCodec(final Uint16TypeDefinition typeDef) {
        super(Uint16.class, typeDef);
    }

    @Override
    protected Uint16 deserialize(final String stringRepresentation, final int base) {
        return Uint16.valueOf(stringRepresentation, base);
    }
}
