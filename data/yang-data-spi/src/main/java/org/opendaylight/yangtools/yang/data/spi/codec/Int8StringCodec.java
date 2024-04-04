/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */

package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.data.api.codec.Int8Codec;
import org.opendaylight.yangtools.yang.model.api.type.Int8TypeDefinition;

final class Int8StringCodec extends AbstractIntegerStringCodec<Byte, Int8TypeDefinition> implements Int8Codec<String> {
    Int8StringCodec(final Int8TypeDefinition typeDef) {
        super(Byte.class, typeDef);
    }

    @Override
    protected Byte deserialize(final String stringRepresentation, final int base) {
        return Byte.valueOf(stringRepresentation, base);
    }
}
