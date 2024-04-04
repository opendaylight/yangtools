/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.data.spi.codec;

import org.opendaylight.yangtools.yang.data.api.codec.Int32Codec;
import org.opendaylight.yangtools.yang.model.api.type.Int32TypeDefinition;

final class Int32StringCodec extends AbstractIntegerStringCodec<Integer, Int32TypeDefinition>
        implements Int32Codec<String> {
    Int32StringCodec(final Int32TypeDefinition typeDef) {
        super(Integer.class, typeDef);
    }

    @Override
    protected Integer deserialize(final String stringRepresentation, final int base) {
        return Integer.valueOf(stringRepresentation, base);
    }
}
